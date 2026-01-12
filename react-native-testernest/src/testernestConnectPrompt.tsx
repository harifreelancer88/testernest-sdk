import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Modal, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { connectTester, init, isConnected } from './native';

export type TesternestConnectPromptProps = {
  publicKey: string;
  baseUrl?: string;
  enableLogs?: boolean;
  title?: string;
  subtitle?: string;
  appName?: string;
  showSkipButton?: boolean;
};

const styles = {
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', padding: 24 },
  card: { backgroundColor: '#fff7ec', borderRadius: 16, padding: 20 },
  title: { fontSize: 20, fontWeight: '700', color: '#1f1b16' },
  subtitle: { marginTop: 8, fontSize: 14, color: '#4b3f35' },
  appName: { marginTop: 6, fontSize: 14, fontWeight: '600', color: '#4b3f35' },
  input: {
    marginTop: 16,
    borderWidth: 1,
    borderColor: '#d4c2ae',
    borderRadius: 10,
    padding: 10,
    backgroundColor: '#fff',
  },
  error: { marginTop: 8, color: '#b00020', fontSize: 13 },
  buttonRow: { flexDirection: 'row', marginTop: 16 },
  skipButton: { flex: 1, marginRight: 8, padding: 12, borderRadius: 10, backgroundColor: '#efe0d1' },
  skipText: { textAlign: 'center', color: '#4b3f35', fontWeight: '600' },
  connectButton: { flex: 1, marginLeft: 8, padding: 12, borderRadius: 10, backgroundColor: '#1f6d5f' },
  connectText: { textAlign: 'center', color: '#fff', fontWeight: '700' },
};

export function TesternestConnectPrompt({
  publicKey,
  baseUrl,
  enableLogs = false,
  title = 'Connect Tester',
  subtitle = 'Enter your 6-digit code to connect.',
  appName,
  showSkipButton = true,
}: TesternestConnectPromptProps) {
  const [visible, setVisible] = useState(false);
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [connecting, setConnecting] = useState(false);
  const dismissedRef = useRef(false);

  const isSixDigit = useMemo(() => /^\d{6}$/.test(code.trim()), [code]);

  useEffect(() => {
    dismissedRef.current = false;
  }, [publicKey, baseUrl]);

  useEffect(() => {
    let active = true;
    if (!publicKey) {
      return undefined;
    }
    (async () => {
      try {
        await init({ publicKey, baseUrl, enableLogs });
        const connected = await isConnected();
        if (active && !connected && !dismissedRef.current) {
          setVisible(true);
        }
      } catch (e) {
        if (active) {
          setError(e instanceof Error ? e.message : String(e));
          setVisible(true);
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [publicKey, baseUrl, enableLogs]);

  const handleSkip = () => {
    dismissedRef.current = true;
    setVisible(false);
  };

  const pollConnected = () => {
    const startedAt = Date.now();
    const interval = setInterval(async () => {
      try {
        const connected = await isConnected();
        if (connected) {
          clearInterval(interval);
          dismissedRef.current = true;
          setVisible(false);
          setConnecting(false);
          return;
        }
      } catch (e) {
        clearInterval(interval);
        setConnecting(false);
        setError(e instanceof Error ? e.message : String(e));
        return;
      }
      if (Date.now() - startedAt > 8000) {
        clearInterval(interval);
        setConnecting(false);
        setError('Unable to connect. Check the code and try again.');
      }
    }, 400);
  };

  const handleConnect = async () => {
    if (connecting) return;
    if (!isSixDigit) {
      setError('Enter a 6-digit code.');
      return;
    }
    setError(null);
    setConnecting(true);
    try {
      await connectTester(code.trim());
      pollConnected();
    } catch (e) {
      setConnecting(false);
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  return (
    <Modal transparent visible={visible} animationType="fade" onRequestClose={handleSkip}>
      <View style={styles.backdrop}>
        <View style={styles.card}>
          <Text style={styles.title}>{title}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
          {appName ? <Text style={styles.appName}>{appName}</Text> : null}
          <TextInput
            style={styles.input}
            value={code}
            onChangeText={setCode}
            placeholder="123456"
            keyboardType="number-pad"
          />
          {error ? <Text style={styles.error}>{error}</Text> : null}
          <View style={styles.buttonRow}>
            {showSkipButton ? (
              <TouchableOpacity style={styles.skipButton} onPress={handleSkip}>
                <Text style={styles.skipText}>Not now</Text>
              </TouchableOpacity>
            ) : null}
            <TouchableOpacity style={styles.connectButton} onPress={handleConnect}>
              <Text style={styles.connectText}>{connecting ? 'Connecting...' : 'Connect'}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

import React, { useEffect, useMemo, useState } from 'react';
import { SafeAreaView, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import {
  init,
  track,
  flush,
  connectTester,
  getDebugSnapshot,
  TesternestConnectPrompt,
} from '@testernest/react-native';

const styles = {
  container: { flex: 1, padding: 20, backgroundColor: '#f5f1ea' },
  title: { fontSize: 22, fontWeight: '600', marginBottom: 12, color: '#2b2b2b' },
  label: { marginTop: 12, marginBottom: 6, color: '#444' },
  input: { borderWidth: 1, borderColor: '#d3c8b8', borderRadius: 8, padding: 10, backgroundColor: '#fff' },
  button: { marginTop: 12, padding: 12, backgroundColor: '#1f6d5f', borderRadius: 8 },
  buttonText: { color: '#fff', textAlign: 'center', fontWeight: '600' },
  errorPanel: {
    backgroundColor: '#b00020',
    padding: 10,
    borderRadius: 8,
    marginBottom: 12,
  },
  errorText: { color: '#fff' },
  statusText: { marginTop: 12, color: '#2b2b2b' },
  logContainer: { marginTop: 16, borderWidth: 1, borderColor: '#d3c8b8', borderRadius: 8 },
  logTitle: { padding: 8, backgroundColor: '#efe7da', borderTopLeftRadius: 8, borderTopRightRadius: 8 },
  logScroll: { maxHeight: 180, padding: 8, backgroundColor: '#fff' },
  logLine: { color: '#2b2b2b', marginBottom: 4, fontSize: 12 },
};

export default function App() {
  const [publicKey, setPublicKey] = useState('');
  const [baseUrl, setBaseUrl] = useState('https://myappcrew-tw.pages.dev');
  const [code, setCode] = useState('');
  const [logs, setLogs] = useState([]);
  const [globalError, setGlobalError] = useState(null);
  const [status, setStatus] = useState('Idle');
  const [lastError, setLastError] = useState(null);

  useEffect(() => {
    const handler = (error, isFatal) => {
      const message = error?.message || String(error);
      const stack = error?.stack || '';
      console.error('GlobalError', message, stack);
      setGlobalError({ message, stack, isFatal: Boolean(isFatal) });
    };

    const defaultHandler = global.ErrorUtils?.getGlobalHandler?.();
    if (global.ErrorUtils?.setGlobalHandler) {
      global.ErrorUtils.setGlobalHandler((error, isFatal) => {
        handler(error, isFatal);
        if (defaultHandler) {
          defaultHandler(error, isFatal);
        }
      });
    }

    const rejectionHandler = (event) => {
      const reason = event?.reason || event;
      const message = reason?.message || String(reason);
      const stack = reason?.stack || '';
      console.error('UnhandledRejection', message, stack);
      setGlobalError({ message, stack, isFatal: false });
    };

    if (typeof globalThis?.addEventListener === 'function') {
      globalThis.addEventListener('unhandledrejection', rejectionHandler);
      return () => globalThis.removeEventListener('unhandledrejection', rejectionHandler);
    }
    if (typeof globalThis !== 'undefined') {
      globalThis.onunhandledrejection = rejectionHandler;
    }
    return () => {
      if (typeof globalThis !== 'undefined') {
        globalThis.onunhandledrejection = null;
      }
    };
  }, []);

  const addLog = (line) => {
    setLogs((prev) => [...prev, line]);
  };

  const isSixDigit = useMemo(() => /^\d{6}$/.test(code.trim()), [code]);

  const onInit = async () => {
    addLog('init start');
    setStatus('Initializing...');
    setLastError(null);
    try {
      await init({ publicKey, baseUrl, enableLogs: true });
      addLog('init ok');
      setStatus('Initialized');
    } catch (e) {
      const message = e?.message || String(e);
      const stack = e?.stack || '';
      console.error('init failed', message, stack);
      addLog(`init failed: ${message}`);
      setStatus('Init failed');
      setLastError(message);
      if (stack) {
        addLog(stack);
      }
    }
  };

  const sendEvent = async () => {
    addLog('track start');
    setStatus('Tracking...');
    setLastError(null);
    try {
      await track('unity_test_event', { source: 'unity' });
      addLog('track ok');
      setStatus('Track OK');
    } catch (e) {
      const message = e?.message || String(e);
      const stack = e?.stack || '';
      console.error('track failed', message, stack);
      addLog(`track failed: ${message}`);
      setStatus('Track failed');
      setLastError(message);
      if (stack) {
        addLog(stack);
      }
    }
  };

  const doConnect = async () => {
    addLog('connect start');
    setStatus('Connecting...');
    setLastError(null);
    if (!isSixDigit) {
      const msg = 'connect failed: connectCode must be exactly 6 digits';
      console.error(msg);
      addLog(msg);
      setStatus('Connect failed');
      setLastError(msg);
      return;
    }
    try {
      await connectTester(code.trim());
      addLog('connect ok');
      setStatus('Connected');
    } catch (e) {
      const message = e?.message || String(e);
      const stack = e?.stack || '';
      console.error('connect failed', message, stack);
      addLog(`connect failed: ${message}`);
      setStatus('Connect failed');
      setLastError(message);
      if (stack) {
        addLog(stack);
      }
    }
  };

  const doFlush = async () => {
    addLog('flush start');
    setStatus('Flushing...');
    setLastError(null);
    try {
      await flush();
      addLog('flush ok');
      setStatus('Flush OK');
    } catch (e) {
      const message = e?.message || String(e);
      const stack = e?.stack || '';
      console.error('flush failed', message, stack);
      addLog(`flush failed: ${message}`);
      setStatus('Flush failed');
      setLastError(message);
      if (stack) {
        addLog(stack);
      }
    }
  };

  const doSnapshot = async () => {
    addLog('debugSnapshot start');
    setStatus('Snapshotting...');
    setLastError(null);
    try {
      const snapshot = await getDebugSnapshot();
      const keys = Object.keys(snapshot || {});
      addLog(`debugSnapshot ok: keys=${keys.join(', ') || 'none'}`);
      setStatus('Snapshot OK');
    } catch (e) {
      const message = e?.message || String(e);
      const stack = e?.stack || '';
      console.error('debugSnapshot failed', message, stack);
      addLog(`debugSnapshot failed: ${message}`);
      setStatus('Snapshot failed');
      setLastError(message);
      if (stack) {
        addLog(stack);
      }
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <TesternestConnectPrompt publicKey={publicKey} baseUrl={baseUrl} appName="Testernest Example" />
      <Text style={styles.title}>Testernest RN Example</Text>

      {globalError ? (
        <View style={styles.errorPanel}>
          <Text style={styles.errorText}>Unhandled Error</Text>
          <Text style={styles.errorText}>{globalError.message}</Text>
          {globalError.stack ? <Text style={styles.errorText}>{globalError.stack}</Text> : null}
        </View>
      ) : null}

      {lastError ? (
        <View style={styles.errorPanel}>
          <Text style={styles.errorText}>Last Error</Text>
          <Text style={styles.errorText}>{lastError}</Text>
        </View>
      ) : null}

      <Text style={styles.label}>Base URL</Text>
      <TextInput
        style={styles.input}
        value={baseUrl}
        onChangeText={setBaseUrl}
        placeholder="https://myappcrew-tw.pages.dev"
        autoCapitalize="none"
        autoCorrect={false}
      />

      <Text style={styles.label}>Public Key</Text>
      <TextInput
        style={styles.input}
        value={publicKey}
        onChangeText={setPublicKey}
        placeholder="Public key"
        autoCapitalize="none"
        autoCorrect={false}
      />

      <Text style={styles.label}>Connect Code (6 digits)</Text>
      <TextInput
        style={styles.input}
        value={code}
        keyboardType="number-pad"
        onChangeText={setCode}
        placeholder="123456"
      />

      <TouchableOpacity style={styles.button} onPress={onInit}>
        <Text style={styles.buttonText}>Init</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={sendEvent}>
        <Text style={styles.buttonText}>Track Test Event</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={doFlush}>
        <Text style={styles.buttonText}>Flush</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={doConnect}>
        <Text style={styles.buttonText}>Connect</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.button} onPress={doSnapshot}>
        <Text style={styles.buttonText}>Debug Snapshot</Text>
      </TouchableOpacity>

      <Text style={styles.statusText}>Status: {status}</Text>

      <View style={styles.logContainer}>
        <Text style={styles.logTitle}>Logs</Text>
        <ScrollView style={styles.logScroll}>
          {logs.map((line, index) => (
            <Text key={`${index}-${line}`} style={styles.logLine}>
              {line}
            </Text>
          ))}
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}

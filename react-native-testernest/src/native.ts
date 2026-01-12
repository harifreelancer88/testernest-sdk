import { NativeModules, Platform } from 'react-native';

export type InitOptions = {
  publicKey: string;
  baseUrl?: string;
  enableLogs?: boolean;
};

export type DebugSnapshot = Record<string, any>;

type NativeTesternest = {
  init(options: InitOptions): Promise<void>;
  track(name: string, properties?: Record<string, any> | null): void;
  flush(): Promise<void>;
  setCurrentScreen(screen: string | null): void;
  connectTester(code6: string): Promise<void>;
  disconnectTester(): Promise<void>;
  getDebugSnapshot(): Promise<DebugSnapshot>;
  isConnected(): Promise<boolean>;
};

const LINKING_ERROR =
  `The package '@testernest/react-native' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({
    ios: "- You have run 'pod install'\n",
    default: '- You have rebuilt the app after installing the package\n',
  }) +
  '- You are not using Expo Go\n';

const Testernest = NativeModules.Testernest as NativeTesternest | undefined;

function getModule(): NativeTesternest {
  if (!Testernest) {
    throw new Error(LINKING_ERROR);
  }
  return Testernest;
}

export function init(options: InitOptions): Promise<void> {
  return getModule().init(options);
}

export function track(name: string, properties?: Record<string, any>): void {
  getModule().track(name, properties ?? null);
}

export function flush(): Promise<void> {
  return getModule().flush();
}

export function setCurrentScreen(screen: string | null): void {
  getModule().setCurrentScreen(screen);
}

export function connectTester(code6: string): Promise<void> {
  return getModule().connectTester(code6);
}

export function disconnectTester(): Promise<void> {
  return getModule().disconnectTester();
}

export function getDebugSnapshot(): Promise<DebugSnapshot> {
  return getModule().getDebugSnapshot();
}

export function isConnected(): Promise<boolean> {
  return getModule().isConnected();
}

using UnityEngine;
using Testernest;

public class TesternestDemo : MonoBehaviour
{
    public string publicKey;
    public string baseUrl = "https://myappcrew-tw.pages.dev";
    public string connectCode;
    public bool enableLogs = true;

    private const float ButtonHeight = 36f;
    private const float ButtonPadding = 8f;

    private void OnGUI()
    {
        float y = 10f;
        float width = 260f;
        float height = ButtonHeight;

        if (GUI.Button(new Rect(10f, y, width, height), "Init"))
        {
            Testernest.Testernest.Init(publicKey, baseUrl, enableLogs);
        }
        y += height + ButtonPadding;

        if (GUI.Button(new Rect(10f, y, width, height), "Track Test Event"))
        {
            Testernest.Testernest.Track("unity_test_event", "{\"source\":\"unity\"}");
        }
        y += height + ButtonPadding;

        if (GUI.Button(new Rect(10f, y, width, height), "Flush"))
        {
            Testernest.Testernest.Flush();
        }
        y += height + ButtonPadding;

        if (GUI.Button(new Rect(10f, y, width, height), "Connect (6-DIGIT)"))
        {
            if (IsSixDigitCode(connectCode))
            {
                Testernest.Testernest.ConnectTester(connectCode);
            }
            else
            {
                Debug.LogError("Connect code must be exactly 6 digits.");
            }
        }
        y += height + ButtonPadding;

        if (GUI.Button(new Rect(10f, y, width, height), "Debug Snapshot"))
        {
            Debug.Log(Testernest.Testernest.GetDebugSnapshot());
        }
    }

    private static bool IsSixDigitCode(string code)
    {
        if (string.IsNullOrEmpty(code) || code.Length != 6)
        {
            return false;
        }

        for (int i = 0; i < code.Length; i++)
        {
            if (code[i] < '0' || code[i] > '9')
            {
                return false;
            }
        }

        return true;
    }
}

using UnityEditor;
using UnityEngine;

public class WebGLBuild
{
    public static void Build()
    {
        string outputPath = "WebGLBuild";

        BuildPlayerOptions opts = new BuildPlayerOptions
        {
            scenes = new[] { "Assets/Scenes/HealthMonitorScene.unity" },
            locationPathName = outputPath,
            target = BuildTarget.WebGL,
            options = BuildOptions.None
        };

        BuildPipeline.BuildPlayer(opts);
        Debug.Log("WebGL Build complete: " + outputPath);
    }
}

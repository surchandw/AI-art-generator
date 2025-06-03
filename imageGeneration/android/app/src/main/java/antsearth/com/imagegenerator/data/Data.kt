package antsearth.com.imagegenerator.data

import antsearth.com.imagegenerator.Constants

data class ShoData(var fetched: Boolean = false, var aronbaSho: String)

data class Config (
    var appSubscribed: Int = Constants.APP_SUBSCRIBED
)

//Stability API data section

data class TextPrompt(val text: String)

data class GenerateImageRequest(
    val text_prompts: List<TextPrompt>,
    val cfg_scale: Int,
    val height: Int,
    val width: Int,
    val samples: Int,
    val steps: Int
)

data class GenerateImageResponse(
    val artifacts: List<Artifact>
)

data class Artifact(
    val base64: String
)

data class GenerationIdResponse(
    val id: String
)

data class ErrorResponse(
    val id: String,
    val name: String,
    val errors: List<String>
)

data class VideoGenerationParameters(
    val cfgScale: Float = 1.8f,
    val motionBucketId: Int = 127
)

data class UpscaleParameters(
    val creativity: Float = 0.05f,
    val prompt: String = "",
    val negativePrompt: String = ""
)
package antsearth.com.imagegenerator

class Constants {
    companion object {
        //Stable family image generation model type
        const val StableFamilyUltra: String = "ultra"
        const val StableFamilyCore: String = "core"
        const val StableFamilySD3: String = "sd3"

        //Stable diffusion image generation engine id
        const val StableDiffusionVersion1: String = "stable-diffusion-xl-1024-v1-0"
        const val StableDiffusionVersion6: String = "stable-diffusion-v1-6"

        //Image file type
        const val JPEG: String = "jpeg"
        const val WEBP: String = "webp"
        const val PNG: String = "png"

        //Timeout in seconds
        const val TIMEOUT: Long = 100

        //Origin of image picker
        const val UPSCALE_ORIGIN: String = "Upscale"
        const val IMAGE_TO_VIDEO_ORIGIN: String = "imageToVideo"

        //Temporary image and video file names
        const val VIDEO_FILENAME: String = "generated_video.mp4"
        const val UPSCALE_FILENAME: String = "upscale_image.png"
        const val ENHANCE_FILENAME: String = "enhance_image.png"
        const val TEXT_TO_IMAGE_FILENAME = "text_to_image.png"
        const val IMAGE_DIR = "images"

        //Wait time for fetching video
        const val VIDEO_OR_IMAGE_GENERATION_WAIT_TIME: Long = 15000L

        //Max allowed image size
        const val MAX_ALLOWED_IMAGE_SIZE: Long = 10485760L

        //Return error codes for Stability API
        const val API_SUCCESS: Int = 200
        const val API_VIDEO_OR_IMAGE_GENERATION_IN_PROGRESS: Int = 202
        const val API_INTERNAL_ERROR: Int = 400
        const val API_UNAUTHORIZED_REQUEST: Int = 401
        const val API_REQUEST_FLAGGED_BY_CONTENT_MODERATOR: Int = 403
        const val API_VIDEO_GENERATION_ID_EXPIRED: Int = 404
        //TODO - Check if this can be prevented for all image upload APIs
        const val API_FILE_SIZE_EXCEEDS_MAX_ALLOWED: Int = 413
        const val API_UNSUPPORTED_LANGUAGE: Int = 422
        const val API_MAX_ALLOWED_REQUEST_EXCEEDED: Int = 429
        const val API_SERVER_INTERNAL_ERROR: Int = 500
        //Other error codes -
        const val NO_TEXT_TO_IMAGE_DESCRIPTION: Int = 600
        const val REWARDED_AD_FAILED_TO_LOAD: Int = 601
        const val NO_INTERNET_CONNECTION: Int = 602
        const val NO_ENHANCE_IMAGE_DESCRIPTION: Int = 603
        //Ad related section -
        const val APP_SUBSCRIBED: Int = 1
        const val APP_NOT_SUBSCRIBED: Int = 0
        const val APPLICATION_ID: String = "ca-app-pub-3940256099942544~3347511713"
        const val REWARDED_AD_ID: String = "ca-app-pub-3940256099942544/5224354917"
        const val SECOND_REWARDED_AD_ID: String = "ca-app-pub-3940256099942544/5224354917"
        const val BANNER_AD_ID: String = "ca-app-pub-3940256099942544/9214589741"
    }

}
package com.sg.newfly20

sealed class Models {

    abstract val degreesPerSecond: Float
    abstract val radius: Float
    abstract val height: Float
    abstract val rotationDegrees: Float

    object Fish: Models() {                        //model-201
        override val degreesPerSecond: Float
            get() = 10f
        override val radius: Float
            get() = 0.5f
        override val height: Float
            get() = -0.1f
        override val rotationDegrees: Float
            get() = 180f
    }
    object Book: Models() {                        //model-202
        override val degreesPerSecond: Float
            get() = 10f
        override val radius: Float
            get() = 0.5f
        override val height: Float
            get() = -0.1f
        override val rotationDegrees: Float
            get() = 180f
    }
    object Wolf: Models() {                        //model-203
        override val degreesPerSecond: Float
            get() = 10f
        override val radius: Float
            get() = 0.5f
        override val height: Float
            get() = 0.2f
        override val rotationDegrees: Float
            get() = 180f
    }

    object Mei_Run: Models() {                        //model-204
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.005f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object BorisEntery: Models() {                        //model-205
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }
    object BorisSammba: Models() {                        //model-205A
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object BorisHipHop: Models() {                        //model-205B
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object BorisSwing: Models() {                        //model-205C
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object DancerGirl: Models() {                        //model-206
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object Guppie: Models() {                        //model-206
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.1f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

    object Broaddtail: Models() {                        //model-208
        override val degreesPerSecond: Float
            get() = 20f
        override val radius: Float
            get() = 0.3f
        override val height: Float
            get() = 0.0f
        override val rotationDegrees: Float
            get() = 0f
    }

}
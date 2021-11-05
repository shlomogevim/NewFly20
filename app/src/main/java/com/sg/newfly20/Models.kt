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
            get() = 1.5f
        override val height: Float
            get() = -0.1f
        override val rotationDegrees: Float
            get() = 180f
    }
}
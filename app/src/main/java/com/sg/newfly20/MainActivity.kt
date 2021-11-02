package com.sg.newfly20

import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private val nodes = mutableListOf<RotatingNode>()
    private lateinit var videoRecorder: VideoRecorder
    private var isRecording = false

    private val model = Models.Fish               //model-20
    private val modelResourceId = R.raw.fish
    val animationString="Armature|ArmatureAction"
    private var spaScale=false
    private var thisScale=1.2f



/*    private val model = Models.Ship3                    // dogma
  private val modelResourceId = R.raw.ship3
   val animationString="CINEMA_4D_Main"
   private var spaScale=false
   private var thisScale=.5f*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = fragment as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            loadModelAndAddToSence(hitResult.createAnchor(), modelResourceId)
        }

        videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }
        setupFab()
    }

    private fun setupFab() {
        fab.setOnClickListener {

            fab.setOnLongClickListener {
                isRecording = videoRecorder.toggleRecordingState()
                true
            }
            fab.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP && isRecording) {
                    isRecording = videoRecorder.toggleRecordingState()
                    Toast.makeText(this, "Save video to gallery ... ", Toast.LENGTH_LONG).show()
                    true
                } else false
            }


        }
    }

    private fun loadModelAndAddToSence(anchor: Anchor?, modelResourceId: Int) {
        ModelRenderable.builder()
            .setSource(this, modelResourceId)
            .build()
            .thenAccept { modelRenderable ->
                addNodeToScene(anchor, modelRenderable)
                eliminateDot()
            }.exceptionally {
                Toast.makeText(this, "Error creatind nodes:$it", Toast.LENGTH_LONG).show()
                null
            }

    }

    private fun addNodeToScene(
        anchor: Anchor?,
        modelRenderable: ModelRenderable?
    ) {
        val anchorNode = AnchorNode(anchor)
        val rotatingNode = RotatingNode(model.degreesPerSecond).apply {

            setParent(anchorNode)
        }
        Node().apply {
            renderable = modelRenderable

            setParent(rotatingNode)
            localPosition = Vector3(model.radius, model.height, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees, 0f))
            if (!spaScale) {
                localScale = Vector3(thisScale, thisScale, thisScale)
            }

        }
        arFragment.arSceneView.scene.addChild(anchorNode)
        nodes.add(rotatingNode)
        val animationData = modelRenderable?.getAnimationData(animationString)
        ModelAnimator(animationData, modelRenderable).apply {
            repeatCount = ModelAnimator.INFINITE
            start()
        }
    }

    private fun eliminateDot() {
        arFragment.arSceneView.planeRenderer.isVisible = false
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
    }
}
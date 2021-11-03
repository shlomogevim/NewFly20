package com.sg.newfly20

import android.annotation.SuppressLint
import android.graphics.Color.RED
import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private val nodes = mutableListOf<RotatingNode>()
    private lateinit var videoRecorder: VideoRecorder
    private var isRecording = false
    private fun getCurrentScene() = arFragment.arSceneView.scene
    val viewNodes= mutableListOf<Node>()


    private val model = Models.Fish               //model-201
    private val modelResourceId = R.raw.fish
    val animationString="Armature|ArmatureAction"
    private var spaScale=false
    private var thisScale=1.2f




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = fragment as ArFragment





        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            //loadModelAndAddToSence(hitResult.createAnchor(), modelResourceId)
            var view1: ViewRenderable? =null
            var view2: ViewRenderable? =null
            val arrRend= arrayListOf(view1,view2)

            loadModel { modelRenderable,viewRendable1,viewRendebal2 ->
                addNodeToScene(hitResult.createAnchor(), modelRenderable, viewRendable1,viewRendebal2)
                eliminateDot()
            }
        }

        videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }
        setupFab()
    }

    @SuppressLint("ClickableViewAccessibility")
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


    private fun loadModel(callback: (ModelRenderable, ViewRenderable,ViewRenderable) -> Unit) {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this,modelResourceId)
            .build()
        val viewRenderable1 = ViewRenderable.builder()
            .setView(this, createDeleteButton1())
            .build()
        val viewRenderable2 = ViewRenderable.builder()
            .setView(this, createDeleteButton2())
            .build()

        CompletableFuture.allOf(modelRenderable, viewRenderable1,viewRenderable2)
            .thenAccept {
                callback(modelRenderable.get(), viewRenderable1.get(),viewRenderable2.get())
            }
            .exceptionally {
                Toast.makeText(this, "Error loading model : $it", Toast.LENGTH_LONG).show()
                null
            }

    }

    private fun createDeleteButton1(): Button {
        return Button(this).apply {
            scaleX=.5f
            scaleY=.5f
            text = "I'm fine thank you"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
        }
    }
    private fun createDeleteButton2(): Button {
        return Button(this).apply {
            scaleX=.5f
            scaleY=.5f
            text = "Hi Mr. Fish"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)

        }
    }
  

    private fun addNodeToScene(
        anchor: Anchor?,
        modelRenewable: ModelRenderable?,
        viewRenderable1: ViewRenderable,
        viewRenderable2: ViewRenderable,
    ) {
        val anchorNode = AnchorNode(anchor)
        getCurrentScene().addChild(anchorNode)

        val rotatingNode=RotatingNode(model.degreesPerSecond).apply {
            setParent(anchorNode)
        }
        val modelNode=Node().apply {
            renderable=modelRenewable
            localPosition = Vector3 (model.radius,model.height,0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees,0f))
            if (!spaScale){
                localScale= Vector3(thisScale,thisScale,thisScale)
            }
            setParent(rotatingNode)
        }
        val animationData=modelRenewable?.getAnimationData(animationString)
        ModelAnimator(animationData,modelRenewable).apply {
            repeatCount=ModelAnimator.INFINITE
            start()
        }

        val viewNode=Node().apply {
            renderable=null
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees+90f,0f))
            if (!spaScale){
                localScale= Vector3(thisScale,thisScale,thisScale)
            }
            setParent(modelNode)
        }

        viewNode.renderable=viewRenderable1

        val viewNode1=Node().apply {
            renderable=null
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y*1.2f, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees+90f,0f))
            if (!spaScale){
                localScale= Vector3(thisScale,thisScale,thisScale)
            }
            setParent(modelNode)
        }

        viewNode1.renderable=viewRenderable2

    }

    private fun addNodeToScene1(
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
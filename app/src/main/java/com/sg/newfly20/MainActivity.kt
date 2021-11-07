package com.sg.newfly20

import android.annotation.SuppressLint
import android.content.res.Resources
import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment

    private val model = Models.Fish               //model-201
    private val modelResourceId = R.raw.fish
    val animationString = "Armature|ArmatureAction"
    private var spaScale = false
    private var thisScale = .7f
    val bubleX = 0.8f              //vertical
    val bubleY = -0.7f

    private val nodes = mutableListOf<RotatingNode>()
    private lateinit var videoRecorder: VideoRecorder
    private var isRecording = false
    private fun getCurrentScene() = arFragment.arSceneView.scene
    val viewNodes = mutableListOf<Node>()
    var selectBtn1 = false
    var text1 = ""
    var text2 = ""
    lateinit var textView1: TextView
    lateinit var textView2: TextView

    var anchorGlobal: Anchor? = null

    var modelNodeGlobal: Node? = null
    var viewNode_1: Node? = null
    var viewNode_2: Node? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment

        setupModel()
        getTextView()
        setUpCamera()
        setupButtons()
    }

    private fun setupModel() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            loadModel { modelRenderable, viewRendable1, viewRendable2 ->
                addNodeToScene(
                    hitResult.createAnchor(), modelRenderable, viewRendable1, viewRendable2
                )
                eliminateDot()
            }
        }
    }

    private fun loadModel(callback: (ModelRenderable, ViewRenderable, ViewRenderable) -> Unit) {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, modelResourceId)
            .build()
        val viewRenderable1 = ViewRenderable.builder()
            .setView(this, textView1)
            .build()
        val viewRenderable2 = ViewRenderable.builder()
            .setView(this, textView2)
            .build()


        CompletableFuture.allOf(modelRenderable, viewRenderable1, viewRenderable2)
            .thenAccept {
                callback(modelRenderable.get(), viewRenderable1.get(), viewRenderable2.get())
            }
            .exceptionally {
                Toast.makeText(this, "Error loading model : $it", Toast.LENGTH_LONG).show()
                null
            }

    }

    private fun addNodeToScene(
        anchor: Anchor?,
        modelRenewable: ModelRenderable?,
        viewRenderable1: ViewRenderable,
        viewRenderable2: ViewRenderable
    ) {
        val anchorNode = AnchorNode(anchor)
        getCurrentScene().addChild(anchorNode)
        anchorGlobal = anchor

        val rotatingNode = RotatingNode(model.degreesPerSecond).apply {
            setParent(anchorNode)
        }

        val modelNode = Node().apply {
            renderable = modelRenewable
            localPosition = Vector3(model.radius, model.height, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees, 0f))
            if (!spaScale) {
                localScale = Vector3(thisScale, thisScale, thisScale)
            }
            //  setParent(rotatingNode)
            setParent(anchorNode)
        }
        modelNodeGlobal = modelNode

        val animationData = modelRenewable?.getAnimationData(animationString)
        ModelAnimator(animationData, modelRenewable).apply {
            repeatCount = ModelAnimator.INFINITE
            start()
        }

        viewNode_1 = addView(viewRenderable1)
        viewNode_2 = addView(viewRenderable2)

    }

    private fun addView(viewRenderable: ViewRenderable): Node {
        return Node().apply {
            // renderable = null
            val box = modelNodeGlobal?.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y * bubleX, box.size.y * bubleY)
            localRotation = Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees + 90f, 0f))
            if (!spaScale) {
                localScale = Vector3(thisScale, thisScale, thisScale)
            }
            renderable = viewRenderable

        }
    }

    private fun getTextView() {
        text1 = "     לא מבין למה אתה מסתכל עלי" +
                "\n" + "מה, אין לך מה לעשות" +
                "\n" + "     זה קצת מטריד אותי המבטים שלך" +
                "\n" + "קצת מטריד..."
        textView1 = createTextView(text1)
        text2 = "     עזוב אותי באמאשל'ך" +
                "\n" + "עזוב אותי" +
                "\n" + "     בינינו,איזה ברדק בעולם שלך" +
                "\n" + "אני חוזר לים ..."
        textView2 = createTextView(text2)
    }

    private fun createTextView(text1: String): TextView {
        return TextView(this).apply {
            setBackgroundResource(R.drawable.thinking)
            setLayoutParams(ViewGroup.LayoutParams(300.toPx(), 300.toPx()))
            text = text1
            setTextColor(android.graphics.Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(
                this@MainActivity,
                R.font.a100_gveretlevinalefalefalef_regular
            )
        }
    }

    private fun setupButtons() {
        btn1.setOnClickListener {
            viewNode_1?.setParent(modelNodeGlobal)
        }

        btn2.setOnClickListener {
            viewNode_1?.setParent(null)
            viewNode_2?.setParent(modelNodeGlobal)

        }

    }

    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


    private fun eliminateDot() {
        arFragment.arSceneView.planeRenderer.isVisible = false
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
    }

    private fun setUpCamera() {
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















    /* private fun bubbleView1(): TextView {
       return TextView(this).apply {
           setBackgroundResource(R.drawable.thinking)
           setLayoutParams(ViewGroup.LayoutParams(300.toPx(), 300.toPx()))
           *//*text =   "       איזה ברדק"+
                    "\n"+"  אני חוזר לים"*//*
            text = "     עזוב אותי באמאשל'ך" +
                    "\n" + "עזוב אותי" +
                    "\n" + "     איזה ברדק בעולם שלך" +
                    "\n" + "אני חוזר לים ..."
            setTextColor(android.graphics.Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(
                this@MainActivity,
                R.font.a100_gveretlevinalefalefalef_regular
            )
        }
    }*/
    /*  private fun bubbleView2(): TextView {
          return TextView(this).apply {
              setBackgroundResource(R.drawable.thinking)
              setLayoutParams(ViewGroup.LayoutParams(300.toPx(), 300.toPx()))
              *//*text =   "       איזה ברדק"+
                    "\n"+"  אני חוזר לים"*//*
            text = "     עזוב אותי באמאשל'ך" +
                    "\n" + "עזוב אותי" +
                    "\n" + "     איזה ברדק בעולם שלך" +
                    "\n" + "אני חוזר לים ..."
            setTextColor(android.graphics.Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(
                this@MainActivity,
                R.font.a100_gveretlevinalefalefalef_regular
            )
        }
    }*/

    /*  private fun messageView(): Button {
          return Button(this).apply {

              text = "עזוב אותי באמאשל'ך" +
                      "\n" + "עזוב אותי" +
                      "\n" + "איזה ברדק בעולם שלך" +
                      "\n" + "אני חוזר לים ..."
              // setBackgroundColor(android.graphics.Color.RED)
              setTextColor(android.graphics.Color.WHITE)
              setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
              // setLayoutParams(ViewGroup.LayoutParams(300.toPx(),300.toPx()))


          }
      }
  */
    /* private fun bubbleView(): ImageButton {
           return ImageButton(this).apply {
               setBackgroundResource(R.drawable.thinking)
               setLayoutParams(ViewGroup.LayoutParams(300.toPx(),300.toPx()))
               setScaleType(ImageView.ScaleType.FIT_XY)
           }
       }*/

    /*  private fun addNodeToScene(
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

            //  setParent(rotatingNode)
              setParent(anchorNode)
           }

           val animationData=modelRenewable?.getAnimationData(animationString)
           ModelAnimator(animationData,modelRenewable).apply {
               repeatCount=ModelAnimator.INFINITE
               start()
           }

          *//* val viewNode=Node().apply {
            renderable=null

            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(box.size.y*messageZ, box.size.y*messageX, box.size.y*messageY)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees+90f,0f))
            if (!spaScale){
                localScale= Vector3(thisScale,thisScale,thisScale)
            }
            setParent(modelNode)
        }
        viewNode.renderable=viewRenderable1*//*

        val viewNode1=Node().apply {
            renderable=null
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3( 0f, box.size.y*bubleX, box.size.y*bubleY)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees+90f,0f))
            if (!spaScale){
                localScale= Vector3(thisScale,thisScale,thisScale)
            }
            setParent(modelNode)

        }

        viewNode1.renderable=viewRenderable2
       btn1.setOnClickListener {
           if (selectBtn1==false) {
               getCurrentScene().removeChild(anchorNode)
               selectBtn1=true
           }else{
               isRecording==false
           }
       }

    }

*/


}
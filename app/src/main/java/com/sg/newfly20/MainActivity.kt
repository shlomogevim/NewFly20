package com.sg.newfly20

import android.annotation.SuppressLint
import android.content.res.Resources
import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
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
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

   val rotatePosition = false
   // val rotatePosition = true

   // private val model = Models1.Fish               //model-201

   // private val model = Models1.Book           //model-202

   // private val model = Models1.Happy_Baby       //model-203

   //  private val model = Models1.Leopard_Run        //model-204

  //   private val model = Models1.Borise_Entry        //model-205

  //  private val model = Models1.Borise_Samba       //model-205A

   // private val model = Models1.Borise_HipHop         //model-205B

   // private val model = Models1.Borise_Swing           //model-205C

  //  private val model = Models1.DanceGirl           //model-206

   // private val model = Models1.Guppie           //model-207

   // private val model = Models1.Broaddtail           //model-208

    private val model = Models1.MrManWalking          //model-209






    val verticalDisplacementFactor = model.verticalViewFactor
    val horizontalDisplacementFactor = model.horizontalViewFactor
    val textviewWight = model.textW
    val textviewHight = model.textH
    val modelScale = model.scale
    val fontSize = model.fontS
    val fontFamily = model.fontFamily
    val modelResourceId = model.modelResourceId
    val animationString = model.animationString


    lateinit var arFragment: ArFragment
    private fun getCurrentScene() = arFragment.arSceneView.scene
    val viewNodes = mutableListOf<Node>()
    private var startAngle: Float = 0f
    private val nodes = mutableListOf<RotatingNode>()
    private lateinit var videoRecorder: VideoRecorder
    private var isRecording = false

    var text1 = ""
    var text2 = ""
    lateinit var textView1: TextView
    lateinit var textView2: TextView

    var anchorGlobal: Anchor? = null

    var modelNodeGlobal: Node? = null
    var viewNode_1: Node? = null
    var viewNode_2: Node? = null

    var but1_On = false
    var but2_On = false
    var but3_On = false
    var but4_On = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment

        setupModel()
        getTextView()
        setUpCamera()
        setupButtons()
        getCurrentScene().addOnUpdateListener {
            rotateViewNodesTowardsUser()
        }

        if (rotatePosition) {
            startAngle = model.startAngelRotation
        } else {
            startAngle = model.startAngelStatic
        }


    }

    private fun setupModel() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            anchorGlobal = hitResult.createAnchor()
            loadModel { modelRenderable, viewRendable1, viewRendable2 ->
                addNodeToScene(
                    anchorGlobal, modelRenderable, viewRendable1, viewRendable2
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


        val rotatingNode = RotatingNode(model.degreesPerSecond).apply {
            setParent(anchorNode)
        }

        val modelNode = Node().apply {
            renderable = modelRenewable
            localPosition = Vector3(model.radius, model.height, 0f)
            localRotation =
                Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees + startAngle, 0f))
            //  if (!spaScale) {
            localScale = Vector3(modelScale, modelScale, modelScale)
            //   }
            if (rotatePosition) {
                setParent(rotatingNode)
            } else {
                setParent(anchorNode)
            }
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
            val box = modelNodeGlobal?.renderable?.collisionShape as Box
            localPosition = Vector3(
                0f,
                box.size.y * verticalDisplacementFactor,
                box.size.y * horizontalDisplacementFactor
            )
            localRotation = Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees + 90f, 0f))
            //if (!spaScale) {
            localScale = Vector3(modelScale, modelScale, modelScale)
            //  }
            renderable = viewRenderable
        }
    }

    private fun rotateViewNodesTowardsUser() {
        for (node in viewNodes) {
            node.renderable?.let {
                val camPos = getCurrentScene().camera.worldPosition
                val viewNodePos = node.worldPosition
                val dir = Vector3.subtract(camPos, viewNodePos)
                node.worldRotation = Quaternion.lookRotation(dir, Vector3.up())
            }
        }
    }

    private fun getTextView() {
        text1 = "  תגיד למה אתה מסתכל עלי כל הזמן?" +
                "\n" + "      ובכלל איפה הכיוון של הים? " +
                "\n" + "      ב'ואנה , אני שוחה כאן כבר חצי שעה " +
                "\n" + "   ונראה לי שאני שוחה די במעגל כל הזמן ..." +
                "\n" + "\n"
        textView1 = createTextView(text1)
        text2 = "\n" + "       ומה זה כל הברדק הזה בחדר שלך ?" +
                "\n" + "     בו'אנה תן לחזור לים הגדול" +
                "\n" + "               שם הרבה יותר נעים לי..." +
                "\n" + "\n"
        textView2 = createTextView(text2)
    }

    private fun createTextView(text1: String): TextView {
        return TextView(this).apply {
            setBackgroundResource(R.drawable.thiking10)
            setLayoutParams(ViewGroup.LayoutParams(textviewHight.toPx(), textviewWight.toPx()))
            text = text1
            setTextColor(android.graphics.Color.BLACK)
            // setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            textSize = fontSize
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(
                this@MainActivity,
                fontFamily
            )
        }
    }

    private fun setupButtons() {
        btn1.setOnClickListener {
            if (!but1_On) {
                viewNode_1?.setParent(modelNodeGlobal)
                viewNode_1?.let { it -> viewNodes.add(it) }
                but1_On = true
            } else {
                viewNode_1?.setParent(null)
                but1_On = false
            }
        }
        btn2.setOnClickListener {
            if (!but2_On) {
                viewNode_2?.setParent(modelNodeGlobal)
                viewNode_2?.let { it -> viewNodes.add(it) }
                but2_On = true
            } else {
                viewNode_2?.setParent(null)
                but2_On = false
            }
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
            isRecording = videoRecorder.toggleRecordingState()
            true
        }

        fab.setOnLongClickListener {
            if (isRecording) {
                isRecording = videoRecorder.toggleRecordingState()
                Toast.makeText(this, "Save video to gallery ... ", Toast.LENGTH_LONG).show()
                true
            } else false
        }

    }

    /*  private fun getTextView() {
       text1 = "          תגיד למה אתה מסתכל עלי כל הזמן?" +
               "\n" + "     מה לא ראית דג בחיים שלך ?" +
               "\n" + "      ובכלל איפה הכיוון של הים? " +
               "\n" + "        פעם אחרונה שאני משאיר את הוויז שלי בבית שלי " +
               "\n" + "      ב'ואנה ,  אני שוחה כאן כבר חצי שעה " +
               "\n" + "   ונראה לי שאני שוחה די במעגל כל הזמן ..."+
               "\n"+ "\n"
       textView1 = createTextView(text1)
       text2 = "                 בכלל מה זה המקום הזה ?" +
               "\n" + "                          ומי זה כל האנשים העצובים האלה מסביב ?" +
               "\n" + "               ומה זה כל הברדק הזה בחדר שלך ?" +
               "\n" + "     בו'אנה תן לחזור לים הגדול" +
               "\n" + "               שם הרבה יותר נעים לי..."+
               "\n"+ "\n"
       textView2 = createTextView(text2)
   }*/

    /* private fun setupButtons() {
         btn1.setOnClickListener {

             viewNode_1?.setParent(modelNodeGlobal)
             viewNode_1?.let { it1 -> viewNodes.add(it1) }
         }

         btn2.setOnClickListener {
             viewNode_1?.setParent(null)
             viewNode_2?.setParent(modelNodeGlobal)

         }
         btn3.setOnClickListener {
             viewNode_2?.setParent(null)

         }

     }*/

    /* @SuppressLint("ClickableViewAccessibility")
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
     }*/


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
                   localScale= Vector3(modelScale,modelScale,modelScale)
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
                localScale= Vector3(modelScale,modelScale,modelScale)
            }
            setParent(modelNode)
        }
        viewNode.renderable=viewRenderable1*//*

        val viewNode1=Node().apply {
            renderable=null
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3( 0f, box.size.y*verticalViewFactor, box.size.y*horizontalViewFactor)
            localRotation = Quaternion.eulerAngles(Vector3(0f,model.rotationDegrees+90f,0f))
            if (!spaScale){
                localScale= Vector3(modelScale,modelScale,modelScale)
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
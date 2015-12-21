import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import saito.objloader.*; 
import java.util.ArrayList; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Assignment3_Subdivision extends PApplet {






OBJModel model;
float rotX;
float rotY;
PVector focus;
boolean wireFrame = true;
boolean bounding = true;
ArrayList displayVertices = new ArrayList();;
ArrayList displayFaces = new ArrayList();;
boolean catmulClark = false;
PVector light = new PVector(500,500,0);
public void setup()
{
    size(600,600, P3D);
    noStroke();
    createGUI();
    focus = new PVector(0,0,0);
}

public void draw()
{
    background(255);
    lights();

    pushMatrix();
    translate(width/2+focus.x,height/2+focus.y,0+focus.z);
    rotateX(rotY);
    rotateY(rotX);
    
    
    if(model != null){
//      model.draw();
    
    
      for (int j = 0; j < model.getSegmentCount(); j++) {
        if(!bounding){
          Segment segment = model.getSegment(j);
          Face[] faces = segment.getFaces();
  
          noFill();
          stroke(0xff252633);
  
          beginShape(QUADS);
  
          for(int i = 0; i < faces.length; i ++)
          {
              PVector[] vs = ( PVector[])faces[i].getVertices();
              //PVector[] ns = faces[i].getNormals();
  
              for (int k = 0; k < vs.length; k++) {
                  //normal(ns[k].x, ns[k].y, ns[k].z);
                  vertex(vs[k].x, vs[k].y, vs[k].z);
              }
          }
  
          endShape();
        }

        fill(0xff989393);
        if(wireFrame){
          stroke(0xff252633);    
        }
        else{
         noStroke(); 
        }

        beginShape(QUADS);

        for(int i = 0; i < displayFaces.size(); i ++)
        {
            PVector[] v = ( PVector[])displayFaces.get(i);//faces[i].getVertices();
            //PVector n = faces[i].getNormal();   
            float nor =0;//abs(sin(radians((frameCount+i))) * 100);
            PVector vv = PVector.sub(v[1] , v[0]) ; 
            PVector ww = PVector.sub(v[2] , v[0]) ;
            //normal
            PVector N = vv.cross(ww);

            for (int k = 0; k < v.length; k++) {
                normal(N.x,N.y,N.z);
                vertex(v[k].x, v[k].y, v[k].z);
              //  vertex(v[k].x + (n.x*nor), v[k].y + (n.y*nor), v[k].z + (n.z*nor));
            }
        }
        endShape();
    }
    }

    popMatrix();
    
}


public void mouseDragged()
{
  if(mouseButton == LEFT){
    rotX += (mouseX - pmouseX) * 0.01f;
    rotY -= (mouseY - pmouseY) * 0.01f;
  }
   else if (mouseButton == CENTER)
    {
        focus.x += 0.7f * (mouseX - pmouseX);
        focus.y += 0.7f * (mouseY - pmouseY);
    }
    else if (mouseButton == RIGHT)
    {
        focus.z += 0.9f * (mouseY - pmouseY);
    } 
}

public void keyPressed(){
  if(key == '+' || key == '='){
   linearSubdivision(); 
   if(catmulClark){
      catmulClarkAveraging();
   }
   else{
     averaging();
   }
  }
  if(key == 's' || key == 'S'){
   linearSubdivision(); 
  }
  if(key == 'a' || key == 'A'){
   if(catmulClark){
     //println("schemeList1");
      catmulClarkAveraging();
   }
   else{
     averaging();
   }
  }
  if(key == 'w' || key == 'W'){
   wireFrame = !wireFrame;
  }
  if(key == 'b' || key == 'B'){
   bounding = !bounding;
  }
  
}
   
public void loadObject(String text){
   // making an object called "model" that is a new instance of OBJModel
    text = text+(".obj");
    model = new OBJModel(this, text, "relative", QUADS);
    // turning on the debug output (it's all the stuff that spews out in the black box down the bottom)
    model.enableDebug();
    model.scale(70);  
    focus = new PVector(0.0f,0.0f,0.0f);
    rotX = 0;
    rotY = 0;
   displayVertices = new ArrayList();
   displayFaces = new ArrayList();
   for (int j = 0; j < model.getVertexCount(); j++) {
     displayVertices.add(model.getVertex(j));
    }
   Face[] faces = null;
   Segment segment;
   for (int j = 0; j < model.getSegmentCount(); j++) {
     segment = model.getSegment(j);
     faces = segment.getFaces();
    }
   for (int j = 0; j <faces.length; j++) {
     displayFaces.add(faces[j].getVertices());
   }
}

/*
Linear Subdivision
*/

ArrayList vertices;
ArrayList newFaces;
ArrayList centroidAll;
public void linearSubdivision(){
  //new vertix
   vertices = new ArrayList();
   newFaces = new ArrayList();
  for (int j = 0; j < displayVertices.size(); j++) {
   vertices.add(displayVertices.get(j));//model.getVertex(j));
  }
  Face[] faces = null;
  Segment segment;
  for (int j = 0; j < model.getSegmentCount(); j++) {
   segment = model.getSegment(j);
   faces = segment.getFaces();
  }
  centroidAll = new ArrayList();
  for(int i=0; i< displayFaces.size(); i ++){
    PVector[] v = (PVector[]) displayFaces.get(i);//faces[i].getVertices();
     PVector centroid = findCentroid(v);
     vertices.add(centroid);
    //create new vertix
    int[] index = new int[v.length];
    for(int j = 0; j < v.length; j++){
      if((j+1)== v.length){
        index[j] = getVert(v[j],v[0]);
      }
      else{
        index[j] = getVert(v[j],v[j+1]);
      } 
    }
    for(int j=0; j< v.length; j ++){
      PVector[] newFace = new PVector[4];
      newFace[0] = v[j];
      newFace[1] = (PVector)vertices.get(index[j]);
      newFace[2] = centroid;
      int ejm1 = j-1;
      if(ejm1 < 0){
        ejm1 = v.length -1;
      }
      newFace[3] = (PVector)vertices.get(index[ejm1]);
      newFaces.add(newFace);
    }
    centroidAll.add(centroid);
  }
  displayVertices = new ArrayList();
  displayVertices = vertices;
  displayFaces = new ArrayList();
  displayFaces = newFaces;
}


/*
Averaging
*/

public void averaging(){
  //declare avgVert and valence
  PVector[] avgVert = new PVector[displayVertices.size()];
  int[] val =  new int[displayVertices.size()];
  for(int i=0; i<displayVertices.size() ; i++){
    avgVert[i] = new PVector(0.0f,0.0f,0.0f); //(PVector)checkIfCentroid((PVector)displayVertices.get(i));//
    val[i] = 0;
  }
  
  ArrayList faceIndex = new ArrayList();
  int[] indexArray;
  //find the centroid and add to the avgVert, and divide by valence
  for(int i=0; i< displayFaces.size(); i ++){
    PVector[] v = (PVector[]) displayFaces.get(i);
    PVector centroid = findCentroid(v);

    indexArray = new int[v.length];
    
    for(int k=0; k< v.length; k++){
      int index =0;
      //if(displayVertices.contains(v[k])){
       index =  getIndex(v[k]);//displayVertices.indexOf(v[k]);
       indexArray[k] = index;
     // }
      avgVert[index].add(centroid);
      val[index] += 1;
    }
    faceIndex.add(indexArray);
  }
  
   for(int i=0; i< displayVertices.size(); i ++){
     avgVert[i].div(val[i]);
   }
   
    //surrounding edge midpoints
   //calculateVertexNeigh(faceIndex);
   
   displayVertices = new ArrayList();
   for(int i=0; i< avgVert.length; i ++){
    // println(avgVert[i]);
     displayVertices.add(avgVert[i]);
   }
   
   int noOfFaces = displayFaces.size();
   PVector[] newVertsOfFaces;
   displayFaces = new ArrayList();
   for(int i=0; i< noOfFaces; i ++){
     int[] indexForFaces = (int[])faceIndex.get(i);
     newVertsOfFaces = new PVector[indexForFaces.length];
     for(int j =0; j<indexForFaces.length ; j++){
       newVertsOfFaces[j] = (PVector)displayVertices.get(indexForFaces[j]);
     }
     displayFaces.add(newVertsOfFaces);
   }
}

public void catmulClarkAveraging(){
  //declare avgVert and valence
  PVector[] avgVert = new PVector[displayVertices.size()];
  PVector[] avgEdgeVert = new PVector[displayVertices.size()];
  
  int[] val =  new int[displayVertices.size()];
  for(int i=0; i<displayVertices.size() ; i++){
    avgVert[i] = new PVector(0.0f,0.0f,0.0f); 
    avgEdgeVert[i] = new PVector(0.0f,0.0f,0.0f);
    val[i] = 0;
  }
  
  ArrayList faceIndex = new ArrayList();
  int[] indexArray;
  
  //find the centroid and add to the avgVert
  for(int i=0; i< displayFaces.size(); i ++){
    PVector[] v = (PVector[]) displayFaces.get(i);
    PVector centroid = findCentroid(v);

    indexArray = new int[v.length];
    int index =0;
    for(int k=0; k< v.length; k++){
       index =  getIndex(v[k]);
       indexArray[k] = index;
       avgVert[index].add(centroid);
       val[index] += 1;
    }
    faceIndex.add(indexArray);
  }
   
   
   //surrounding edge
   calculateVertexNeigh(faceIndex);
   
    for(int i=0; i<displayVertices.size() ; i++){
      int[] vertNeig = (int[])vertexNeighbours.get(i);
      avgEdgeVert[i] = new PVector(0,0,0);
      for(int j=0; j<vertNeig.length; j++){
        int ind = vertNeig[j];
        //PVector edgeMidPoint = PVector.div(PVector.add((PVector)displayVertices.get(i),(PVector)displayVertices.get(ind)),2.0);
        avgEdgeVert[i].add((PVector)displayVertices.get(ind));//add(edgeMidPoint);
      }
    }
    
   //face points  
   for(int i=0; i< displayVertices.size(); i ++){
     avgVert[i].div(val[i]);
     //avgVert[i].div(val[i]);
     avgEdgeVert[i].div(val[i]);
    // avgEdgeVert[i].div(val[i]);
    // avgEdgeVert[i].mult(2.0);
   }
   
   ArrayList ControlVerts = displayVertices;
   displayVertices = new ArrayList();
   for(int i=0; i< ControlVerts.size(); i ++){
     PVector controlVert = (PVector)ControlVerts.get(i);
     if(val[i] == 3){
       controlVert = new PVector(0,0,0);
     }
     else{
       controlVert.mult((val[i] -3.0f));
     }
     displayVertices.add(PVector.div(PVector.add(PVector.add(avgVert[i],PVector.mult(avgEdgeVert[i],2)),controlVert), val[i]));
   }
   
   int noOfFaces = displayFaces.size();
   PVector[] newVertsOfFaces;
   displayFaces = new ArrayList();
   for(int i=0; i< noOfFaces; i ++){
     int[] indexForFaces = (int[])faceIndex.get(i);
     newVertsOfFaces = new PVector[indexForFaces.length];
     for(int j =0; j<indexForFaces.length ; j++){
       newVertsOfFaces[j] = (PVector)displayVertices.get(indexForFaces[j]);
     }
     displayFaces.add(newVertsOfFaces);
   }
  
}


/*
helper functions
*/

public int getVert(PVector a, PVector b){
  PVector center = PVector.div(PVector.add(a,b),2.0f);
  int index;
  //check if this vector is present in the list
  index = vertices.indexOf(center);
  if(index == -1){
    vertices.add(center);
    index = vertices.indexOf(center);
  }
  return index;
}


public int getIndex(PVector playlist) {
    for (int i = 0; i < displayVertices.size(); i++) {
      PVector bla = (PVector)displayVertices.get(i);
        if ((float)bla.x == (float)playlist.x && (float)bla.y == (float)playlist.y && (float)bla.z == (float)playlist.z) {
          return i;
        }
    }
    return -1;
}

public PVector findCentroid(PVector[] verts){
  PVector centroid = new PVector(0.0f,0.0f,0.0f);
    for(int k=0; k<verts.length; k++){
      centroid.add(verts[k]);
    }
    centroid.div(verts.length);
    return centroid;
}

public PVector checkIfCentroid(PVector playlist) {
  println(centroidAll.size());
    for (int i = 0; i < centroidAll.size(); i++) {
      PVector bla = (PVector)centroidAll.get(i);
     // println(bla.x,playlist.x, bla.y,playlist.y,bla.z,playlist.z);
        if ((float)bla.x == (float)playlist.x && (float)bla.y == (float)playlist.y && (float)bla.z == (float)playlist.z) {
          return ((PVector)playlist);
        }
    }
    return (new PVector(0.0f,0.0f,0.0f));
}

ArrayList vertexNeighbours;

public void calculateVertexNeigh(ArrayList faceIndex){
  vertexNeighbours = new ArrayList();
  for(int j=0; j< displayVertices.size(); j++){
    ArrayList eachVertsNeg = new ArrayList();
    for(int i=0; i< faceIndex.size(); i ++){
      int[] indices = (int[])faceIndex.get(i);
      for(int k=0; k<indices.length; k++){
       if(j ==  indices[k]){
         if((k+1)==indices.length){
           if(!eachVertsNeg.contains(indices[0])){
             eachVertsNeg.add((int)indices[0]);
           }
         }
         else{
           if(!eachVertsNeg.contains(indices[k+1])){
             eachVertsNeg.add((int)indices[k+1]);
           }
         }
       }
      }
    }
    //print("j:"+j);
    int[] temp = new int[eachVertsNeg.size()];
    for(int k = 0; k< eachVertsNeg.size(); k++){
      temp[k] = (Integer) (eachVertsNeg.get(k));
      //print(" ,"+temp[k]);
    }
    //println("..");
    vertexNeighbours.add(temp);
  }
  
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */
/*
synchronized public void win_draw1(GWinApplet appc, GWinData data) { //_CODE_:GUI:651654:
  appc.background(230);
} //_CODE_:GUI:651654:

public void dropList1_click1(GDropList source, GEvent event) { //_CODE_:objList:305444:
  println("objList - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:objList:305444:

public void button1_click1(GButton source, GEvent event) { //_CODE_:button1:894643:
  println("button1 - GButton event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:button1:894643:

public void dropList1_click2(GDropList source, GEvent event) { //_CODE_:schemeList1:924018:
  println("schemeList1 - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:schemeList1:924018:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  if(frame != null)
    frame.setTitle("Sketch Window");
  GUI = new GWindow(this, "GUI Window", 0, 0, 240, 335, false, JAVA2D);
  GUI.addDrawHandler(this, "win_draw1");
  objList = new GDropList(GUI.papplet, 7, 25, 101, 105, 5);
  objList.setItems(loadStrings("list_305444"), 0);
  objList.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  objList.addEventHandler(this, "dropList1_click1");
  button1 = new GButton(GUI.papplet, 132, 20, 80, 30);
  button1.setText("Load Obj");
  button1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  button1.addEventHandler(this, "button1_click1");
  schemeList1 = new GDropList(GUI.papplet, 8, 118, 100, 48, 2);
  schemeList1.setItems(loadStrings("list_924018"), 0);
  schemeList1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  schemeList1.addEventHandler(this, "dropList1_click2");
  label1 = new GLabel(GUI.papplet, 11, 169, 213, 150);
  label1.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  label1.setText("My label");
  label1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  label1.setOpaque(false);
  label2 = new GLabel(GUI.papplet, 130, 117, 92, 30);
  label2.setText("Select Method");
  label2.setOpaque(false);
}

// Variable declarations 
// autogenerated do not edit
GWindow GUI;
GDropList objList; 
GButton button1; 
GDropList schemeList1; 
GLabel label1; 
GLabel label2; 
*/
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

synchronized public void win_draw1(GWinApplet appc, GWinData data) { //_CODE_:GUI:651654:
  appc.background(230);
} //_CODE_:GUI:651654:

public void dropList1_click1(GDropList source, GEvent event) { //_CODE_:objList:305444:
  //println("objList - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:objList:305444:

public void button1_click1(GButton source, GEvent event) { //_CODE_:button1:894643:
 // println("button1 - GButton event occured " + System.currentTimeMillis()%10000000 );
  loadObject(objList.getSelectedText());
} //_CODE_:button1:894643:

public void dropList1_click2(GDropList source, GEvent event) { //_CODE_:schemeList1:924018:
  //println("schemeList1 - GDropList event occured " + System.currentTimeMillis()%10000000 );
  if(schemeList1.getSelectedIndex () == 0){
    catmulClark = false;
  }
  else{
    catmulClark = true;
  }
} //_CODE_:schemeList1:924018:

// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  if(frame != null)
    frame.setTitle("Sketch Window");
  GUI = new GWindow(this, "GUI Window", 0, 0, 240, 335, false, JAVA2D);
  GUI.addDrawHandler(this, "win_draw1");
  objList = new GDropList(GUI.papplet, 7, 25, 101, 105, 5);
  objList.setItems(loadStrings("list_305444"), 0);
  objList.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  objList.addEventHandler(this, "dropList1_click1");
  button1 = new GButton(GUI.papplet, 132, 20, 80, 30);
  button1.setText("Load Obj");
  button1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  button1.addEventHandler(this, "button1_click1");
  
  schemeList1 = new GDropList(GUI.papplet, 8, 118, 100, 48, 2);
  schemeList1.setItems(loadStrings("list_924018"), 0);
  schemeList1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  schemeList1.addEventHandler(this, "dropList1_click2");
  
  label1 = new GLabel(GUI.papplet, 11, 169, 213, 150);
  label1.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  label1.setText("ASSIGNMENT 3 - SUBDIVISION \n '+' - subdivide & average \n 's'- subdivide only \n 'a'- average only \n 'w' toggle for wireframe \n 'b' toggle for original wireframe");
  label1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  label1.setOpaque(false);
  
  label2 = new GLabel(GUI.papplet, 130, 117, 92, 30);
  label2.setText("Select Method");
  label2.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  label2.setOpaque(false);
}

// Variable declarations 
// autogenerated do not edit
GWindow GUI;
GDropList objList; 
GButton button1; 
GDropList schemeList1; 
GLabel label1; 
GLabel label2; 
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Assignment3_Subdivision" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

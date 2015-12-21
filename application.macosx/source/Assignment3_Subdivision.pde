import g4p_controls.*;
import saito.objloader.*;
import java.util.ArrayList;


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
void setup()
{
    size(600,600, P3D);
    noStroke();
    createGUI();
    focus = new PVector(0,0,0);
}

void draw()
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
          stroke(#252633);
  
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

        fill(#989393);
        if(wireFrame){
          stroke(#252633);    
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


void mouseDragged()
{
  if(mouseButton == LEFT){
    rotX += (mouseX - pmouseX) * 0.01;
    rotY -= (mouseY - pmouseY) * 0.01;
  }
   else if (mouseButton == CENTER)
    {
        focus.x += 0.7 * (mouseX - pmouseX);
        focus.y += 0.7 * (mouseY - pmouseY);
    }
    else if (mouseButton == RIGHT)
    {
        focus.z += 0.9 * (mouseY - pmouseY);
    } 
}

void keyPressed(){
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
   
void loadObject(String text){
   // making an object called "model" that is a new instance of OBJModel
    text = text+(".obj");
    model = new OBJModel(this, text, "relative", QUADS);
    // turning on the debug output (it's all the stuff that spews out in the black box down the bottom)
    model.enableDebug();
    model.scale(70);  
    focus = new PVector(0.0,0.0,0.0);
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
void linearSubdivision(){
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

void averaging(){
  //declare avgVert and valence
  PVector[] avgVert = new PVector[displayVertices.size()];
  int[] val =  new int[displayVertices.size()];
  for(int i=0; i<displayVertices.size() ; i++){
    avgVert[i] = new PVector(0.0,0.0,0.0); //(PVector)checkIfCentroid((PVector)displayVertices.get(i));//
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

void catmulClarkAveraging(){
  //declare avgVert and valence
  PVector[] avgVert = new PVector[displayVertices.size()];
  PVector[] avgEdgeVert = new PVector[displayVertices.size()];
  
  int[] val =  new int[displayVertices.size()];
  for(int i=0; i<displayVertices.size() ; i++){
    avgVert[i] = new PVector(0.0,0.0,0.0); 
    avgEdgeVert[i] = new PVector(0.0,0.0,0.0);
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
       controlVert.mult((val[i] -3.0));
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

int getVert(PVector a, PVector b){
  PVector center = PVector.div(PVector.add(a,b),2.0);
  int index;
  //check if this vector is present in the list
  index = vertices.indexOf(center);
  if(index == -1){
    vertices.add(center);
    index = vertices.indexOf(center);
  }
  return index;
}


int getIndex(PVector playlist) {
    for (int i = 0; i < displayVertices.size(); i++) {
      PVector bla = (PVector)displayVertices.get(i);
        if ((float)bla.x == (float)playlist.x && (float)bla.y == (float)playlist.y && (float)bla.z == (float)playlist.z) {
          return i;
        }
    }
    return -1;
}

PVector findCentroid(PVector[] verts){
  PVector centroid = new PVector(0.0,0.0,0.0);
    for(int k=0; k<verts.length; k++){
      centroid.add(verts[k]);
    }
    centroid.div(verts.length);
    return centroid;
}

PVector checkIfCentroid(PVector playlist) {
  println(centroidAll.size());
    for (int i = 0; i < centroidAll.size(); i++) {
      PVector bla = (PVector)centroidAll.get(i);
     // println(bla.x,playlist.x, bla.y,playlist.y,bla.z,playlist.z);
        if ((float)bla.x == (float)playlist.x && (float)bla.y == (float)playlist.y && (float)bla.z == (float)playlist.z) {
          return ((PVector)playlist);
        }
    }
    return (new PVector(0.0,0.0,0.0));
}

ArrayList vertexNeighbours;

void calculateVertexNeigh(ArrayList faceIndex){
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

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

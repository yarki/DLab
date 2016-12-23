package DataModel;

public class CreateNotebookDto {
    
    private String name;
    private String shape;
    private String version;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getShape() {
        return shape;
    }
    
    public void setShape(String shape) {
        this.shape = shape;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public CreateNotebookDto(String name, String shape, String version){
        this.name = name;
        this.shape = shape;
        this.version = version;
    }
    
    public CreateNotebookDto(){
        
    }

}
       
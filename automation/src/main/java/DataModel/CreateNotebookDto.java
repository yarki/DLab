package DataModel;

public class CreateNotebookDto {
    
	private String image;
    private String name;
    private String shape;
    private String version;
    

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

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
       
import { ResourceShapeModel } from "./resourceShape.model";
import { ComputationalResourceApplicationTemplate } from "./computationalResourceApplicationTemplate.model";
import { ImageType } from "./imageType.enum";

export class ComputationalResourceImage {
  template_name: string;
  description: string;
  environment_type: ImageType;
  shapes: Array<ResourceShapeModel>;
  application_templates: Array<ComputationalResourceApplicationTemplate>

  constructor(jsonModel:any) {
    this.template_name = jsonModel.template_name;
    this.description = jsonModel.description;
    this.environment_type = ImageType.СOMPUTATIONAL;
    this.application_templates = [];
    this.shapes = [];

    if(jsonModel.computation_resources_shapes && jsonModel.computation_resources_shapes.length > 0)
      for (let index = 0; index < jsonModel.computation_resources_shapes.length; index++)
        this.shapes.push(new ResourceShapeModel(jsonModel.computation_resources_shapes[index]));

    if(jsonModel.templates && jsonModel.templates.length > 0)
      for (let index = 0; index < jsonModel.templates.length; index++)
        this.application_templates.push(new ComputationalResourceApplicationTemplate(jsonModel.templates[index]));

  }
}

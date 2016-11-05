import { ComputationalResourceShapeModel } from "./computationalResourceShape.model";
import { ComputationalResourceApplicationTemplate } from "./computationalResourceApplicationTemplate.model";
import { ImageType } from "./imageType.enum";


export class ComputationalResourceImage {
  template_name: string;
  description: string;
  environment_type: ImageType;
  shapes: Array<ComputationalResourceShapeModel>;
  application_templates: Array<ComputationalResourceApplicationTemplate>
}

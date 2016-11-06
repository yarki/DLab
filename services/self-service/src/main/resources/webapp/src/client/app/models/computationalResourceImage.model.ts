import { ResourceShapeModel } from "./resourceShape.model";
import { ComputationalResourceApplicationTemplate } from "./computationalResourceApplicationTemplate.model";
import { ImageType } from "./imageType.enum";

export class ComputationalResourceImage {
  template_name: string;
  description: string;
  environment_type: ImageType;
  shapes: Array<ResourceShapeModel>;
  application_templates: Array<ComputationalResourceApplicationTemplate>

  fromJSON() : void { }
}

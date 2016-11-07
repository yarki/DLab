import { ImageType } from "./imageType.enum";
import { ResourceShapeModel } from './resourceShape.model';

export class ExploratoryEnvironmentVersionModel {
  template_name: string;
  description: string;
  environment_type: ImageType;
  version: string;
  vendor: string;

  shapes: Array<ResourceShapeModel>;

  constructor(jsonModel:any, shapes: Array<ResourceShapeModel>) {
    this.template_name = jsonModel.template_name;
    this.description = jsonModel.description;
    this.environment_type = ImageType.EXPLORATORY;
    this.version = jsonModel.version;
    this.vendor = jsonModel.vendor;
    this.shapes = shapes;
  }
}

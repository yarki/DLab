import { ResourceShapeModel } from "./resourceShape.model";
import { ExploratoryEnvironmentVersionModel } from "./exploratoryEnvironmentVersion.model";
import { ImageType } from "./imageType.enum";

export class ComputationalResourceImage {
  environment_type: ImageType;
  exploratory_environment_shapes: Array<ResourceShapeModel>;
  exploratory_environment_versions: Array<ExploratoryEnvironmentVersionModel>;

  fromJSON() : void { }
}

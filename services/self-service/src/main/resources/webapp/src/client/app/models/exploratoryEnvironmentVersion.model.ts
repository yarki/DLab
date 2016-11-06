import { ImageType } from "./imageType.enum";

export class ExploratoryEnvironmentVersionModel {
  template_name: string;
  description: string;
  environment_type: ImageType;
  version: string;
  vendor: string;

  fromJSON(): void{ }
}

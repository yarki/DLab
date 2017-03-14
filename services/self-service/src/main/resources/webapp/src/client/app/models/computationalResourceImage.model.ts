/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { ComputationalResourceApplicationTemplate } from './computationalResourceApplicationTemplate.model';
import { ResourceShapeTypesModel } from './resourceShapeTypes.model';
import { ImageType } from './imageType.enum';
import { SortUtil } from '../util/sortUtil';

export class ComputationalResourceImage {
  template_name: string;
  description: string;
  environment_type: ImageType;
  shapes: ResourceShapeTypesModel;
  application_templates: Array<ComputationalResourceApplicationTemplate>;

  constructor(jsonModel: any) {
    this.template_name = jsonModel.template_name;
    this.description = jsonModel.description;
    this.environment_type = ImageType.СOMPUTATIONAL;
    this.application_templates = [];

    this.shapes = new ResourceShapeTypesModel(SortUtil.shapesSort(jsonModel.computation_resources_shapes));

    if (jsonModel.templates && jsonModel.templates.length > 0)
      for (let index = 0; index < jsonModel.templates.length; index++)
        this.application_templates.push(new ComputationalResourceApplicationTemplate(jsonModel.templates[index], this.shapes));

  }
}

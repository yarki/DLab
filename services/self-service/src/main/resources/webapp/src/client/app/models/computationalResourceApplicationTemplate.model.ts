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

import { ComputationalResourceApplication } from './computationalResourceApplication.model';
import { ResourceShapeTypesModel } from './resourceShapeTypes.model';

export class ComputationalResourceApplicationTemplate {
  version: string;
  applications: Array<ComputationalResourceApplication>;
  shapes: ResourceShapeTypesModel;

  constructor(jsonModel: any, shapes: ResourceShapeTypesModel) {
    this.version = jsonModel.version;
    this.applications = [];
    this.shapes = shapes;

    if (jsonModel.applications && jsonModel.applications.length > 0)
      for (let index = 0; index < jsonModel.applications.length; index++)
        this.applications.push(new ComputationalResourceApplication(jsonModel.applications[index]));

  }
}

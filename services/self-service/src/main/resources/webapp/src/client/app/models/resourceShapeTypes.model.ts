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
import { ResourceShapeModel } from './resourceShape.model';

export class ResourceShapeTypesModel {
  resourcesShapeTypes: any;

  constructor(jsonModel: any) {
    this.resourcesShapeTypes = {};

    for(let parentIndex in jsonModel) {
      if (jsonModel[parentIndex] && jsonModel[parentIndex].length > 0) {
        let tmpl = [];
        for (let index = 0; index < jsonModel[parentIndex].length; index++)
          tmpl.push(new ResourceShapeModel(jsonModel[parentIndex][index]));

        this.resourcesShapeTypes[parentIndex] = tmpl;
      }
    }
  }
}

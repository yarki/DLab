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

  memory_optimized: Array<ResourceShapeModel>;
  gpu_optimized: Array<ResourceShapeModel>;
  compute_optimized: Array<ResourceShapeModel>;

  constructor(jsonModel: any) {
    this.memory_optimized = [];
    this.gpu_optimized = [];
    this.compute_optimized = [];

    if(jsonModel['Memory optimized'] && jsonModel['Memory optimized'].length > 0)
      for (let index = 0; index < jsonModel['Memory optimized'].length; index++)
        this.memory_optimized.push(new ResourceShapeModel(jsonModel['Memory optimized'][index]));

    if(jsonModel['GPU optimized'] && jsonModel['GPU optimized'].length > 0)
      for (let index = 0; index < jsonModel['GPU optimized'].length; index++)
        this.gpu_optimized.push(new ResourceShapeModel(jsonModel['GPU optimized'][index]));

    if(jsonModel['Compute optimized'] && jsonModel['Compute optimized'].length > 0)
      for (let index = 0; index < jsonModel['Compute optimized'].length; index++)
        this.compute_optimized.push(new ResourceShapeModel(jsonModel['Compute optimized'][index]));

  }
}

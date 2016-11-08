/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Observable } from "rxjs";
import { Response } from "@angular/http";
import { UserResourceService } from "../../services/userResource.service";
import { ExploratoryEnvironmentVersionModel } from "../../models/exploratoryEnvironmentVersion.model";
import { ResourceShapeModel } from "../../models/resourceShape.model";
import HTTP_STATUS_CODES from 'http-status-enum';

export class ExploratoryEnvironmentCreateModel {

  confirmAction: Function;
  selectedItemChanged: Function;

  private environment_name: string;
  private environment_version: string;
  private environment_shape: string;
  private userResourceService: UserResourceService;
  private continueWith: Function;

  selectedItem: ExploratoryEnvironmentVersionModel = new ExploratoryEnvironmentVersionModel({}, []);
  exploratoryEnvironmentTemplates: Array<ExploratoryEnvironmentVersionModel> = [];

  constructor(
    environment_name: string,
    environment_version: string,
    environment_shape: string,
    fnProcessResults: any,
    fnProcessErrors: any,
    selectedItemChanged: Function,
    continueWith: Function,
    userResourceService: UserResourceService
  ) {
    this.userResourceService = userResourceService;
    this.selectedItemChanged = selectedItemChanged;
    this.continueWith = continueWith;
    this.prepareModel(environment_name, environment_version, environment_shape, fnProcessResults, fnProcessErrors);
    this.loadTemplates();
  }

  static getDefault(userResourceService): ExploratoryEnvironmentCreateModel {
    return new ExploratoryEnvironmentCreateModel('', '', '', () => { }, () => { }, null, null, userResourceService);
  }

  public setSelectedItem(item: ExploratoryEnvironmentVersionModel) {
    this.selectedItem = item;
  }

  private createExploratoryEnvironment(): Observable<Response> {
    return this.userResourceService.createExploratoryEnvironment({
      name: this.environment_name,
      shape: this.environment_shape,
      version: this.environment_version
    });
  }

  private prepareModel(environment_name: string, environment_version: string, environment_shape: string, fnProcessResults: any, fnProcessErrors: any): void {

    this.setCreatingParams(environment_version, environment_name, environment_shape)
    this.confirmAction = () => this.createExploratoryEnvironment()
      .subscribe(
      (response: Response) => fnProcessResults(response));
  }

  setCreatingParams(version, name, shape) {
    this.environment_version = version;
    this.environment_name = name;
    this.environment_shape = shape;
  }

  loadTemplates() {
    if(this.exploratoryEnvironmentTemplates.length == 0)
      this.userResourceService.getExploratoryEnvironmentTemplates()
        .subscribe(
        data => {
          for(let parentIndex = 0; parentIndex < data.length; parentIndex ++) {

            let shapeJson = data[parentIndex].exploratory_environment_shapes;
            let exploratoryJson = data[parentIndex].exploratory_environment_versions;
            let shapeArr = new Array<ResourceShapeModel>();

            for (let index = 0; index < shapeJson.length; index++)
              shapeArr.push(new ResourceShapeModel(shapeJson[index]));

            for (let index = 0; index < exploratoryJson.length; index++)
              this.exploratoryEnvironmentTemplates.push(new ExploratoryEnvironmentVersionModel(exploratoryJson[index], shapeArr));
          }
          if(this.exploratoryEnvironmentTemplates.length > 0)
            this.setSelectedTemplate(0);

          if(this.continueWith)
            this.continueWith();
        });
  }

  setSelectedTemplate(value) {
    this.selectedItem = this.exploratoryEnvironmentTemplates[value];
    if(this.selectedItemChanged)
      this.selectedItemChanged();
  }
}

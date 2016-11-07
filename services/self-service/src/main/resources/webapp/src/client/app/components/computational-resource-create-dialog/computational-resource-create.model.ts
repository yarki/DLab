/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Observable } from "rxjs";
import { Response } from "@angular/http";
import { UserResourceService } from "../../services/userResource.service";
import { ComputationalResourceImage } from "../../models/computationalResourceImage.model";
import { ResourceShapeModel } from "../../models/resourceShape.model";
import { ComputationalResourceApplicationTemplate } from "../../models/computationalResourceApplicationTemplate.model";
import HTTP_STATUS_CODES from 'http-status-enum';

export class ComputationalResourceCreateModel {

  confirmAction: Function;
  selectedItemChanged: Function;

  computational_resource_alias: string;
  computational_resource_count: number;
  computational_resource_master_shape: string;
  computational_resource_slave_shape: string;

  notebook_name: string;

  private userResourceService: UserResourceService;
  private continueWith: Function;

  selectedItem: ComputationalResourceApplicationTemplate = new ComputationalResourceApplicationTemplate({}, []);
  computationalResourceImages: Array<ComputationalResourceImage> = [];
  computationalResourceApplicationTemplates: Array<ComputationalResourceApplicationTemplate> = [];

  constructor(
    computational_resource_alias: string,
    computational_resource_count: number,
    computational_resource_master_shape: string,
    computational_resource_slave_shape: string,

    fnProcessResults: any,
    fnProcessErrors: any,
    selectedItemChanged: Function,
    continueWith: Function,
    userResourceService: UserResourceService
  ) {
    this.userResourceService = userResourceService;
    this.selectedItemChanged = selectedItemChanged;
    this.continueWith = continueWith;
    this.prepareModel(fnProcessResults, fnProcessErrors);
    this.loadTemplates();
  }

  static getDefault(userResourceService): ComputationalResourceCreateModel {
    return new ComputationalResourceCreateModel('', 0, '', '', () => { }, () => { }, null, null, userResourceService);
  }

  public setSelectedItem(item: ComputationalResourceApplicationTemplate) {
    this.selectedItem = item;
  }

  private createComputationalResource(): Observable<Response> {
    return this.userResourceService.createComputationalResource({
      name: this.computational_resource_alias,
      emr_instance_count: this.computational_resource_count,
      emr_master_instance_type: this.computational_resource_master_shape,
      emr_slave_instance_type: this.computational_resource_slave_shape,
      emr_version: this.selectedItem.version,
      notebook_name: this.notebook_name
    });
  };

  private prepareModel(fnProcessResults: any, fnProcessErrors: any): void {
    this.confirmAction = () => this.createComputationalResource()
      .subscribe(
      (response: Response) => fnProcessResults(response),
      (response: Response) => fnProcessErrors(response)
    );
  }

  setCreatingParams(name: string, count: number, shape_master: string, shape_slave: string) {
    this.computational_resource_alias = name;
    this.computational_resource_count = count;
    this.computational_resource_master_shape = shape_master;
    this.computational_resource_slave_shape = shape_slave;
  }

  loadTemplates() {
    if (this.computationalResourceImages.length == 0)
      this.userResourceService.getComputationalResourcesTemplates()
        .subscribe(
        data => {
          for (let parentIndex = 0; parentIndex < data.length; parentIndex++) {
            let computationalResourceImage = new ComputationalResourceImage(data[parentIndex]);
            this.computationalResourceImages.push(computationalResourceImage);

            for(let index = 0; index < computationalResourceImage.application_templates.length; index++)
              this.computationalResourceApplicationTemplates.push(computationalResourceImage.application_templates[index]);
          }
          if (this.computationalResourceImages.length > 0)
            this.setSelectedTemplate(0);

          if (this.continueWith)
            this.continueWith();
        });
  }

  setSelectedTemplate(index: number) {
    this.selectedItem = this.computationalResourceApplicationTemplates[index];

    if (this.selectedItemChanged)
      this.selectedItemChanged();
  }
}

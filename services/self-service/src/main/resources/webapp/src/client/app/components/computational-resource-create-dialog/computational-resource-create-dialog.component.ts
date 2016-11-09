/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { UserResourceService } from "../../services/userResource.service";
import { Response } from "@angular/http";
import { ComputationalResourceCreateModel } from "./computational-resource-create.model";
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'computational-resource-create-dialog',
  templateUrl: 'computational-resource-create-dialog.component.html'
})

export class ComputationalResourceCreateDialog {

  model: ComputationalResourceCreateModel;
  notebook_instance: any;
  computationalResourceExist: boolean = false;
  clusterNamePattern: string = "\\w+.*\\w+";

  @ViewChild('bindDialog') bindDialog;
  @ViewChild('name') name;
  @ViewChild('count') count;

  @Output() buildGrid: EventEmitter<{}> = new EventEmitter();

  constructor(private userResourceService: UserResourceService) {
    this.model = ComputationalResourceCreateModel.getDefault(userResourceService);
  }

  ngOnInit(){
    this.bindDialog.onClosing =  () => this.resetDialog();
  }

  public createComputationalResource_btnClick($event, name: string, count: number, shape_master: string, shape_slave: string) {
    this.computationalResourceExist = false;

    if (this.containsComputationalResource(name)) {
      this.computationalResourceExist = true;
      return false;
    }

    this.model.setCreatingParams(name, count, shape_master, shape_slave);
    this.model.confirmAction();
    $event.preventDefault();
    return false;
  }

  public templateSelectionChange(value) {
    this.model.setSelectedTemplate(value);
  }

  public containsComputationalResource(conputational_resource_name: string): boolean {
    if(conputational_resource_name)
      for (var index = 0; index < this.notebook_instance.resources.length; index++)
        if (conputational_resource_name.toLowerCase() == this.notebook_instance.resources[index].computational_name.toString().toLowerCase())
            return true;

      return false;
  }

  public open(params, notebook_instance) {
    if (!this.bindDialog.isOpened) {
      this.notebook_instance = notebook_instance;

      this.model = new ComputationalResourceCreateModel('', 0, '', '', notebook_instance.name, (response: Response) => {
        if (response.status === HTTP_STATUS_CODES.OK) {
          this.computationalResourceExist = false;
          this.close();
          this.buildGrid.emit();
        }
      },
        (response: Response) => console.error(response.status),
        () => {
          // this.templateDescription = this.model.selectedItem.description;
        },
        () => {
          this.bindDialog.open(params);
        },
        this.userResourceService);

    }
  }

  close() {
    if (this.bindDialog.isOpened)
      this.bindDialog.close();
  }

  private resetDialog() : void {
    this.model.resetModel();
    this.name.nativeElement.value = "";
    this.count.nativeElement.value = "";
  }
}

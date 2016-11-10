/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { Response } from "@angular/http";
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { UserResourceService } from "../../services/userResource.service";
import { ExploratoryEnvironmentCreateModel } from './exploratory-environment-create.model';
import { ExploratoryEnvironmentVersionModel } from '../../models/exploratoryEnvironmentVersion.model';
import { ResourceShapeModel } from '../../models/resourceShape.model';

import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'exploratory-environment-create-dialog',
  templateUrl: 'exploratory-environment-create-dialog.component.html'
})

export class ExploratoryEnvironmentCreateDialog {
  model: ExploratoryEnvironmentCreateModel;
  notebookExist: boolean = false;
  checkValidity: boolean = false;
  templateDescription: string;
  namePattern = "\\w+.*\\w+";
  resourceGrid: any;

  public createExploratoryEnvironmentForm: FormGroup;

  @ViewChild('bindDialog') bindDialog;
  @ViewChild('environment_name') environment_name;
  @ViewChild('environment_shape') environment_shape;


  @Output() buildGrid: EventEmitter<{}> = new EventEmitter();

  constructor(
    private userResourceService: UserResourceService,
    private _fb: FormBuilder
  ) {
    this.model = ExploratoryEnvironmentCreateModel.getDefault(userResourceService);
  }

  ngOnInit(){
    this.initFormModel();
    this.bindDialog.onClosing =  () => this.resetDialog();
  }

  initFormModel(): void {
    this.createExploratoryEnvironmentForm = this._fb.group({
      environment_name: ['', [Validators.required, Validators.pattern(this.namePattern)]]
    });
  }

  createExploratoryEnvironment_btnClick($event, data, valid, index, shape) {
    this.notebookExist = false;
    this.checkValidity = true;

    if (this.resourceGrid.containsNotebook(data.environment_name)) {
      this.notebookExist = true;
      return false;
    }

    this.model.setCreatingParams(this.model.exploratoryEnvironmentTemplates[index].version, data.environment_name, shape);
    this.model.confirmAction();
    $event.preventDefault();
    return false;
  }

  templateSelectionChanged(value) {
      this.model.setSelectedTemplate(value);
  }

  open(params) {
    if (!this.bindDialog.isOpened) {
      this.model = new ExploratoryEnvironmentCreateModel('', '', '', (response: Response) => {
        if (response.status === HTTP_STATUS_CODES.OK) {
          this.close();
          this.buildGrid.emit();
        }
      },
        (response: Response) => console.error(response.status),
        () => {
          this.templateDescription = this.model.selectedItem.description;
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
    this.notebookExist = false;
    this.checkValidity = false;

    this.initFormModel();
    this.model.resetModel();
  }
}

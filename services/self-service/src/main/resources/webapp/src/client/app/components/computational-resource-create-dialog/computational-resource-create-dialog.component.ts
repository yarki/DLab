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

import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Response } from '@angular/http';
import { UserResourceService } from '../../services/userResource.service';
import { ComputationalResourceCreateModel } from './computational-resource-create.model';

import { ErrorMapUtils } from './../../util/errorMapUtils';
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'computational-resource-create-dialog',
  templateUrl: 'computational-resource-create-dialog.component.html'
})

export class ComputationalResourceCreateDialog {

  model: ComputationalResourceCreateModel;
  notebook_instance: any;
  shapes: any;
  computationalResourceExist: boolean = false;
  checkValidity: boolean = false;
  clusterNamePattern: string = '[-_a-zA-Z0-9]+';
  nodeCountPattern: string = '^[1-9]\\d*$';

  processError: boolean = false;
  errorMessage: string = '';

  public createComputationalResourceForm: FormGroup;

  @ViewChild('bindDialog') bindDialog;
  @ViewChild('name') name;
  @ViewChild('count') count;
  @ViewChild('templatesList') templates_list;
  @ViewChild('masterShapesList') master_shapes_list;
  @ViewChild('shapesSlaveList') slave_shapes_list;

  @Output() buildGrid: EventEmitter<{}> = new EventEmitter();

  constructor(
    private userResourceService: UserResourceService,
    private _fb: FormBuilder
  ) {
    this.model = ComputationalResourceCreateModel.getDefault(userResourceService);
  }

  ngOnInit() {
    this.initFormModel();
    this.bindDialog.onClosing = () => this.resetDialog();
  }

  public isNumberKey($event): boolean {
    let charCode = ($event.which) ? $event.which : $event.keyCode;
    if (charCode !== 46 && charCode > 31 && (charCode < 48 || charCode > 57)) {
      $event.preventDefault();
      return false;
    }
    return true;
  }

  public onUpdate($event): void {
    if ($event.model.type === 'template') {
      this.model.setSelectedTemplate($event.model.index);
      this.master_shapes_list.setDefaultOptions(this.model.selectedItem.shapes,
        this.model.selectedItem.shapes.Memory_optimized[0].description, 'master_shape', 'description', 'json');
      this.slave_shapes_list.setDefaultOptions(this.model.selectedItem.shapes,
        this.model.selectedItem.shapes.Memory_optimized[0].description, 'slave_shape', 'description', 'json');
    }

    if (this.shapes[$event.model.type])
      this.shapes[$event.model.type] = $event.model.value.type;
  }

  public createComputationalResource($event, data, shape_master: string, shape_slave: string) {
    this.computationalResourceExist = false;
    this.checkValidity = true;

    if (this.containsComputationalResource(data.cluster_alias_name)) {
      this.computationalResourceExist = true;
      return false;
    }

    this.model.setCreatingParams(data.cluster_alias_name, data.instance_number, shape_master, shape_slave);
    this.model.confirmAction();
    $event.preventDefault();
    return false;
  }

  public containsComputationalResource(conputational_resource_name: string): boolean {
    if (conputational_resource_name)
      for (var index = 0; index < this.notebook_instance.resources.length; index++)
        var computational_name = this.notebook_instance.resources[index].computational_name.toString().toLowerCase();

        if (conputational_resource_name.toLowerCase() === computational_name)
          return true;

    return false;
  }

  public open(params, notebook_instance): void {
    if (!this.bindDialog.isOpened) {
      this.notebook_instance = notebook_instance;

      this.model = new ComputationalResourceCreateModel('', 0, '', '', notebook_instance.name, (response: Response) => {
        if (response.status === HTTP_STATUS_CODES.OK) {
          this.computationalResourceExist = false;
          this.close();
          this.buildGrid.emit();
        }
      },
        (response: Response) => {
          this.processError = true;
          this.errorMessage = ErrorMapUtils.setErrorMessage(response);
        },
        () => {
          // this.templateDescription = this.model.selectedItem.description;
        },
        () => {
          this.bindDialog.open(params);
          this.setDefaultParams();
        },
        this.userResourceService);

    }
  }

  public close(): void {
    if (this.bindDialog.isOpened)
      this.bindDialog.close();
  }

  private initFormModel(): void {
    this.createComputationalResourceForm = this._fb.group({
      cluster_alias_name: ['', [Validators.required, Validators.pattern(this.clusterNamePattern)]],
      instance_number: ['1', [Validators.required, Validators.pattern(this.nodeCountPattern)]]
    });
  }

  private setDefaultParams(): void {
    this.shapes = {
      master_shape: this.model.selectedItem.shapes.Memory_optimized[0].type,
      slave_shape: this.model.selectedItem.shapes.Memory_optimized[0].type
    };
    this.templates_list.setDefaultOptions(this.model.computationalResourceApplicationTemplates,
      this.model.selectedItem.version, 'template', 'version', 'array');
    this.master_shapes_list.setDefaultOptions(this.model.selectedItem.shapes,
      this.model.selectedItem.shapes.Memory_optimized[0].description, 'master_shape', 'description', 'json');
    this.slave_shapes_list.setDefaultOptions(this.model.selectedItem.shapes,
      this.model.selectedItem.shapes.Memory_optimized[0].description, 'slave_shape', 'description', 'json');
  }

  private resetDialog(): void {
    this.computationalResourceExist = false;
    this.checkValidity = false;
    this.processError = false;
    this.errorMessage = '';

    this.initFormModel();
    this.model.resetModel();
  }
}

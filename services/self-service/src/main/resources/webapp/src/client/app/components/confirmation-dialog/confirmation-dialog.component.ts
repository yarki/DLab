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

import { Component, ViewChild, Output, EventEmitter } from '@angular/core';
import { UserResourceService } from './../../services/userResource.service';
import { HealthStatusService } from './../../services/healthStatus.service';
import { ConfirmationDialogModel } from './confirmation-dialog.model';
import { ConfirmationDialogType } from './confirmation-dialog-type.enum';
import { Response } from '@angular/http';

import { ErrorMapUtils } from './../../util/errorMapUtils';
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'confirmation-dialog',
  templateUrl: 'confirmation-dialog.component.html'
})

export class ConfirmationDialog {
  model: ConfirmationDialogModel;
  isAliveResources: boolean;
  processError: boolean = false;
  errorMessage: string = '';

  @ViewChild('bindDialog') bindDialog;
  @Output() buildGrid: EventEmitter<{}> = new EventEmitter();

  constructor(
    private userResourceService: UserResourceService,
    private healthStatusService: HealthStatusService
  ) {
    this.model = ConfirmationDialogModel.getDefault();
  }

  ngOnInit() {
    this.bindDialog.onClosing = () => this.resetDialog();
  }

  public open(param, notebook: any, type: ConfirmationDialogType) {
    this.model = new ConfirmationDialogModel(type, notebook, (response: Response) => {
      if (response.status === HTTP_STATUS_CODES.OK) {
        this.close();
        this.buildGrid.emit();
      }
    },
      (response: Response) => {
        this.processError = true;
        this.errorMessage = ErrorMapUtils.setErrorMessage(response);
      },
      this.userResourceService,
      this.healthStatusService);

    this.bindDialog.open(param);
    this.isAliveResources = this.model.isAliveResources(notebook.resources);
  }

  public close() {
    this.bindDialog.close();
  }

  private resetDialog(): void {
    this.processError = false;
    this.errorMessage = '';
  }
}

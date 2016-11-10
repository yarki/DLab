/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import { UserResourceService } from "./../../services/userResource.service";
import {ConfirmationDialogModel} from "./confirmation-dialog.model";
import {ConfirmationDialogType} from "./confirmation-dialog-type.enum";
import {Response} from "@angular/http";

import { ErrorMapUtils } from './../../util/errorMapUtils';
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'confirmation-dialog',
  templateUrl: 'confirmation-dialog.component.html'
})

export class confirmationDialog {
  model: ConfirmationDialogModel;

  processError: boolean = false;
  errorMessage: string = '';

  @ViewChild('bindDialog') bindDialog;
  @Output() buildGrid: EventEmitter<{}> = new EventEmitter();

  constructor(private userResourceService: UserResourceService) {
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
      this.userResourceService);

    this.bindDialog.open(param);
  }

  public close() {
    this.bindDialog.close();
  }

  private resetDialog(): void {
    this.processError = false;
    this.errorMessage = '';
  }
}

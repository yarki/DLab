/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { Response } from "@angular/http";

import { KeyUploadDialogModel } from './key-upload.model';
import {UserAccessKeyService} from "../../services/userAccessKey.service";
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'key-upload-dialog',
  templateUrl: 'key-upload-dialog.component.html'
})

export class UploadKeyDialog {
  model: KeyUploadDialogModel;

  @ViewChild('bindDialog') bindDialog;
  @ViewChild('userAccessKeyUploadControl') userAccessKeyUploadControl;
  @Output() checkInfrastructureCreationProgress: EventEmitter<{}> = new EventEmitter();
  constructor(private userAccessKeyService : UserAccessKeyService) {
    this.model = KeyUploadDialogModel.getDefault();
  }

  uploadUserAccessKey_onChange($event) {
    if ($event.target.files.length > 0) {
      this.model.setUserAccessKey($event.target.files[0]);
    }
  }

  uploadUserAccessKey_btnClick($event) {
    this.model.confirmAction();
    $event.preventDefault();
    return false;
  }

  open(params) {
    if(!this.bindDialog.isOpened)
    {
      this.model = new KeyUploadDialogModel(null, (response: Response) => {
          if (response.status === HTTP_STATUS_CODES.OK) {
            this.close();
            this.checkInfrastructureCreationProgress.emit();
          }
        },
        (response: Response) => console.error(response.status),
        this.userAccessKeyService);

      this.bindDialog.open(params);
    }
  }

  close() {
    this.userAccessKeyUploadControl.nativeElement.value = "";
    if(this.bindDialog.isOpened)
      this.bindDialog.close();
  }
}

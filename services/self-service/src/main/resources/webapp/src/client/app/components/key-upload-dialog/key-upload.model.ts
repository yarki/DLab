/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/
import {Observable} from "rxjs";
import {Response} from "@angular/http";
import {UserAccessKeyService} from "../../services/userAccessKey.service";
import HTTP_STATUS_CODES from 'http-status-enum';

export class KeyUploadDialogModel {

  confirmAction: Function;
  private userAccessKeyService : UserAccessKeyService;

  private accessKeyLabel: string;
  private accessKeyFormValid: boolean;
  private newAccessKeyForUpload: File;

  constructor(
    newAccessKeyForUpload: any,
    fnProcessResults: any,
    fnProcessErrors: any,
    userAccessKeyService : UserAccessKeyService
  ) {
    this.userAccessKeyService = userAccessKeyService;
    this.prepareModel(newAccessKeyForUpload, fnProcessResults, fnProcessErrors);
  }

  static getDefault(): KeyUploadDialogModel {
    return new KeyUploadDialogModel(null, () => { }, () => { }, null);
  }

  private uploadUserAccessKey(): Observable<Response> {
    let formData = new FormData();
    formData.append("file", this.newAccessKeyForUpload);

    return this.userAccessKeyService.uploadUserAccessKey(formData);
  }

  private prepareModel(newAccessKeyForUpload: any, fnProcessResults: any, fnProcessErrors: any): void {
    this.setUserAccessKey(newAccessKeyForUpload);
    this.confirmAction = () => this.uploadUserAccessKey()
      .subscribe(
      (response: Response) => fnProcessResults(response));
  }

  private getLabel(file : File): string {
    if (file)
      return !this.accessKeyFormValid ? ".pub file is required." : file.name;
    return '';
  }

  private isValidKey(value): boolean {
    if (value.toLowerCase().endsWith(".pub"))
      return true;
    return false;
  }

  setUserAccessKey(accessKey: File) : void {
    if(accessKey && (this.accessKeyFormValid = this.isValidKey(accessKey.name))) {

      this.newAccessKeyForUpload = accessKey;
      this.accessKeyLabel = this.getLabel(this.newAccessKeyForUpload)
    }

  }
}

/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, OnInit, ViewChild } from '@angular/core';

import {UserAccessKeyService} from "../services/userAccessKey.service";
import {UserResourceService} from "../services/userResource.service";
import { Grid } from '../components/grid/grid.component';
import HTTP_STATUS_CODES from 'http-status-enum';

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: []
})

export class HomeComponent implements OnInit {
  private readonly CHECK_ACCESS_KEY_TIMEOUT : number = 10000;

  userUploadAccessKeyState: number;
  createTempls: any;
  shapes: any;
  emrTempls: any;
  notebookExist: boolean = false;
  progressDialogConfig: any;

  templateDescription: string;

  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;
  @ViewChild('createAnalyticalModal') createAnalyticalModal;
  @ViewChild(Grid) grid: Grid;

  // -------------------------------------------------------------------------
  // Overrides
  // --

  constructor(
    private userAccessKeyService: UserAccessKeyService,
    private userResourceService: UserResourceService
  ) {
    this.userUploadAccessKeyState = HTTP_STATUS_CODES.NOT_FOUND;
  }

  ngOnInit() {
    this.checkInfrastructureCreationProgress();
    this.initAnalyticSelectors();

    this.progressDialogConfig = this.setProgressDialogConfiguration();
  }

  createNotebook_btnClick() {
    this.processAccessKeyStatus(this.userUploadAccessKeyState, true);
  }

  refreshGrid() {
    this.grid.buildGrid();
  }

  private checkInfrastructureCreationProgress() {
    this.userAccessKeyService.checkUserAccessKey()
      .subscribe(
      response => this.processAccessKeyStatus(response.status, false),
      error => this.processAccessKeyStatus(error.status, false)
      );
  }

  private toggleDialogs(keyUploadDialogToggle, preloaderDialogToggle, createAnalyticalToolDialogToggle) {

    if (keyUploadDialogToggle) {
      if (!this.keyUploadModal.isOpened)
        this.keyUploadModal.open({ isFooter: false });
    }
    else {
      if (this.keyUploadModal.isOpened)
        this.keyUploadModal.close();
    }

    if (preloaderDialogToggle)
      this.preloaderModal.open({ isHeader: false, isFooter: false });
    else
      this.preloaderModal.close();

    if (createAnalyticalToolDialogToggle) {
      if (!this.createAnalyticalModal.isOpened)
        this.createAnalyticalModal.open({ isFooter: false });
    }
    else {
      if (this.createAnalyticalModal.isOpened)
        this.createAnalyticalModal.close();
    }
  }

  private processAccessKeyStatus(status: number, forceShowKeyUploadDialog: boolean) {
    this.userUploadAccessKeyState = status;

    if (status == HTTP_STATUS_CODES.NOT_FOUND) // key haven't been uploaded
      this.toggleDialogs(true, false, false);
    else if (status == HTTP_STATUS_CODES.ACCEPTED) { // Key uploading
      this.toggleDialogs(false, true, false);
      setTimeout(() => this.checkInfrastructureCreationProgress(), this.CHECK_ACCESS_KEY_TIMEOUT)
    } else if (status == HTTP_STATUS_CODES.OK && forceShowKeyUploadDialog)
      this.toggleDialogs(false, false, true);
    else if (status == HTTP_STATUS_CODES.OK) // Key uploaded
      this.toggleDialogs(false, false, false);

  }

  initAnalyticSelectors() {
    this.userResourceService.getExploratoryEnvironmentTemplates()
      .subscribe(
      data => {
        let arr = [];
        let str = JSON.stringify(data);
        let dataArr = JSON.parse(str);
        dataArr.forEach((obj, index) => {
          let versions = obj.templates.map((versionObj, index) => {
            return versionObj.version;
          });
          delete obj.templates;
          versions.forEach((version, index) => {
            arr.push(Object.assign({}, obj))
            arr[index].version = version;
          })
        });
        this.createTempls = arr;
      },
      error => this.createTempls = []
      );

    this.userResourceService.getComputationalResourcesTemplates()
      .subscribe(
      data => {
        let arr = [];
        let str = JSON.stringify(data);
        let dataArr = JSON.parse(str);
        dataArr.forEach((obj, index) => {
          let versions = obj.templates.map((versionObj, index) => {
            return versionObj.version;
          });
          delete obj.templates;
          versions.forEach((version, index) => {
            arr.push(Object.assign({}, obj))
            arr[index].version = version;
          })
        });
        this.emrTempls = arr;
      },
      error => this.emrTempls = []
      );

    this.userResourceService.getSupportedResourcesShapes()
      .subscribe(
      data => {
        this.shapes = data
      },
      error => this.shapes = []
      );
  }

  createUsernotebook(event, tmplIndex, name, shape) {
    this.notebookExist = false;
    event.preventDefault();

    if (this.grid.containsNotebook(name.value)) {
      this.notebookExist = true;
      return false;
    }

    this.userResourceService
      .createExploratoryEnvironment({
        name: name.value,
        shape: shape.value,
        version: this.createTempls[tmplIndex].version
      })
      .subscribe((result) => {
        console.log('result: ', result);

        if (this.createAnalyticalModal.isOpened) {
          this.createAnalyticalModal.close();
        }
        this.grid.buildGrid();
        name.value = "";
        this.notebookExist = false;
      });
  };

  setProgressDialogConfiguration() {
    return {
      message: 'Initial infrastructure is being created, <br/>please, wait...',
      content: '<img src="assets/img/gif-spinner.gif" alt="">',
      modal_size: 'modal-xs',
      text_style: 'info-label',
      aligning: 'text-center'
    }
  }

  showDescription(value) {
    this.templateDescription = this.createTempls[value].description;
  }
}

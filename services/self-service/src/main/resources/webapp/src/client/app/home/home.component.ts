/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, OnInit, ViewChild } from '@angular/core';
import { UserAccessKeyService } from "../services/userAccessKey.service";
import { UserResourceService } from "../services/userResource.service";
import { ResourcesGrid } from '../components/resources-grid/resources-grid.component';

import { ResourceShapeModel } from '../models/resourceShape.model';
import { ExploratoryEnvironmentVersionModel } from '../models/exploratoryEnvironmentVersion.model';

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
  createTempls: Array<ExploratoryEnvironmentVersionModel> = [];
  shapes: Array<ResourceShapeModel> = [];
  emrTempls: any;
  progressDialogConfig: any;

  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;
  @ViewChild('createAnalyticalModal') createAnalyticalModal;
  @ViewChild(ResourcesGrid) resourcesGrid: ResourcesGrid;

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
    this.resourcesGrid.buildGrid();
  }

  private checkInfrastructureCreationProgress() {
    this.userAccessKeyService.checkUserAccessKey()
      .subscribe(
      response => this.processAccessKeyStatus(response.status, false),
      error => this.processAccessKeyStatus(error.status, false)
      );
  }

  private toggleDialogs(keyUploadDialogToggle, preloaderDialogToggle, createAnalyticalToolDialogToggle) {

    if (keyUploadDialogToggle)
        this.keyUploadModal.open({ isFooter: false });
    else
        this.keyUploadModal.close();

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
        let shapes = [], templates = [];
        let dataArr = JSON.parse(JSON.stringify(data));
        dataArr.forEach((obj) => {
          shapes.push(obj.exploratory_environment_shapes);
          templates.push(obj.exploratory_environment_versions);
        });
        this.shapes = [].concat.apply([], shapes);
        this.createTempls = [].concat.apply([], templates);
      }, error => this.createTempls = []);

    this.userResourceService.getComputationalResourcesTemplates()
      .subscribe(
      data => {
        let templates = [];
        let dataArr = JSON.parse(JSON.stringify(data));
        dataArr.forEach((obj) => {
          templates.push(obj.templates);
        });
        this.emrTempls = [].concat.apply([], templates);
      }, error => this.emrTempls = []);
  }

  ifEnvironmentExist(name: string): boolean {
    return this.resourcesGrid.containsNotebook(name);
  }

  setProgressDialogConfiguration() {
    return {
      message: 'Initial infrastructure is being created, <br/>please, wait...',
      content: '<img src="assets/img/gif-spinner.gif" alt="">',
      modal_size: 'modal-xs',
      text_style: 'info-label',
      aligning: 'text-center'
    }
  }
}

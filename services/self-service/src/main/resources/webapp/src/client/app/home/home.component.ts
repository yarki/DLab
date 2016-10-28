/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/

import { Component, OnInit, ViewChild, Input } from '@angular/core';

import {UserAccessKeyService} from "../services/userAccessKey.service";
import {UserResourceService} from "../services/userResource.service";
import {AppRoutingService} from "../routing/appRouting.service";
import { Grid } from '../components/grid/grid.component';
import {AuthenticationService} from "../../../../dist/tmp/app/security/authentication.service";
import {ApplicationSecurityService} from "../services/applicationSecurity.service";

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [ApplicationSecurityService]
})

export class HomeComponent implements OnInit {
  uploadAccessUserKeyFormInvalid: boolean;
  createTempls: any;
  shapes: any;
  emrTempls: any;
  uploadKey: any;
  userAccessKeyUploaded: boolean;
  uploadAccessKeyLabel: string;
  notebookExist: boolean = false;


  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;
  @ViewChild('createAnalyticalModal') createAnalyticalModal;
  @ViewChild(Grid) grid:Grid ;

  // -------------------------------------------------------------------------
  // Overrides
  // --

  constructor(
    private applicationSecurityService: ApplicationSecurityService,
    private userAccessKeyService: UserAccessKeyService,
    private userResourceService: UserResourceService,
    private appRoutingService : AppRoutingService
  ) {
    this.userAccessKeyUploaded = false;
    this.uploadAccessUserKeyFormInvalid = true;
  }

  ngOnInit() {
    this.checkInfrastructureCreationProgress(true);
    this.initAnalyticSelectors();
  }

  //
  // Handlers
  //

  logout_btnClick() {
    this.applicationSecurityService.logout().subscribe(
      () => this.appRoutingService.redirectToLoginPage(),
      error => console.log(error),
      () => this.appRoutingService.redirectToLoginPage());
  }


  uploadUserAccessKey_btnClick(event) {

    let formData = new FormData();
    formData.append("file", this.uploadKey);

    this.userAccessKeyService.uploadUserAccessKey(formData)
    .subscribe(
      response => {
        if(response.status === 200)
          this.checkInfrastructureCreationProgress(false);
      },
      error => console.log(error)
     );

     event.preventDefault();
  }

  uploadUserAccessKey_onChange($event) {
    this.uploadAccessKeyLabel = "";

    if($event.target.files.length > 0)
    {
      let fileName = $event.target.files[0].name;
      this.uploadAccessUserKeyFormInvalid = !fileName.toLowerCase().endsWith(".pub");
      this.uploadKey = $event.target.files[0];
      this.uploadAccessKeyLabel = this.uploadAccessUserKeyFormInvalid ? ".pub file is required." : fileName;
    }
  }

  refreshGrid() {
    this.grid.buildGrid();
  }

  //
  // Private Methods
  //

  private checkInfrastructureCreationProgress(callOnce: boolean) {
    this.userAccessKeyService.checkUserAccessKey()
      .subscribe(
      response => {
        if(response.status == 200)
        {
          this.userAccessKeyUploaded = true;
          if (this.preloaderModal.isOpened) {
            this.preloaderModal.close();
          }
        } else if (response.status == 202)
        {
          if (this.keyUploadModal.isOpened)
            this.keyUploadModal.close();

          if (!this.preloaderModal.isOpened)
            this.preloaderModal.open({ isHeader: false, isFooter: false });

          if(!callOnce)
            setTimeout(() => this.checkInfrastructureCreationProgress(false), 10000)
        }
      },
      err => {
        if (err.status == 404) // key haven't been uploaded
        {
          if (!this.keyUploadModal.isOpened)
            this.keyUploadModal.open({ isFooter: false });

          if(!callOnce)
            setTimeout(() => this.checkInfrastructureCreationProgress(false), 10000)
        }
        else {
            console.error(err);
        }
      }
      );
  }

  initAnalyticSelectors() {
    this.userResourceService.getCreateTmpl()
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
        error => this.createTempls = [{template_name: "Jupiter box"}, {template_name: "Jupiter box"}]
      );

    this.userResourceService.getEmrTmpl()
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
        error => this.emrTempls = [{template_name: "Jupiter box"}, {template_name: "Jupiter box"}]
      );

    this.userResourceService.getShapes()
      .subscribe(
        data => {
          this.shapes = data
        },
        error => this.shapes = [{shape_name: 'M4.large'}, {shape_name: 'M4.large'}]
      );
  }

  createUsernotebook(event, tmplIndex, name, shape){
    event.preventDefault();

    this.grid.list.forEach(function(notebook){
      if(name.value.toLowerCase() === notebook.environment_name.toLowerCase()) {
        this.notebookExist = true;
        return false;
      }
    }, this);


    this.userResourceService
      .createUsernotebook({
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

}

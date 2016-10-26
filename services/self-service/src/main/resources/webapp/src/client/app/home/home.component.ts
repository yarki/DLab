/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/

import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from './../security/authentication.service';
import {UserAccessKeyService} from "../services/userAccessKey.service";
import {UserResourceService} from "../services/userResource.service";
import {AppRoutingService} from "../routing/appRouting.service";
import {Http, Response} from '@angular/http';
import { Grid } from '../components/grid/grid.component';

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [AuthenticationService]
})

export class HomeComponent implements OnInit {
  key: any;
  keyName: string;
  uploadAccessKeyUrl: string;
  preloadModalInterval: any;
  uploadAccessUserKeyFormInvalid: boolean;
  createTempls: any;
  shapes: any;
  emrTempls: any;
  uploadKey: any;



  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;
  @ViewChild(Grid) refresh:Grid ;

  // -------------------------------------------------------------------------
  // Overrides
  // --

  constructor(
    private authenticationService: AuthenticationService,
    private userAccessKeyService: UserAccessKeyService,
    private userResourceService: UserResourceService,
    private appRoutingService : AppRoutingService,
    private http: Http
  ) {
    this.uploadAccessUserKeyFormInvalid = true;
    this.uploadAccessKeyUrl = this.userAccessKeyService.getAccessKeyUrl();
  }

  ngOnInit() {
    this.checkInfrastructureCreationProgress();
    this.initAnalyticSelectors();
  }

  //
  // Handlers
  //

  logout_btnClick() {
    this.authenticationService.logout().subscribe(
      () => this.appRoutingService.redirectToLoginPage(),
      error => console.log(error),
      () => this.appRoutingService.redirectToLoginPage());
  }


  uploadUserAccessKey_btnClick(event) {
    this.keyUploadModal.close();
    this.preloaderModal.open({ isHeader: false, isFooter: false });

    let formData = new FormData();
    formData.append("file", this.uploadKey);

    this.userResourceService.uploadKey(formData)
    .subscribe((data) => console.log(data));

    
     this.preloadModalInterval = setInterval(function() {
      this.checkInfrastructureCreationProgress();  
     }.bind(this), 10000);
     event.preventDefault();
  }

  uploadUserAccessKey_onChange($event) {
    this.uploadKey = $event.srcElement.files[0];
    if($event.target.files.length > 0)
    {
      let fileName = $event.target.files[0].name;
      this.uploadAccessUserKeyFormInvalid = !fileName.toLowerCase().endsWith(".pub");
      if(!this.uploadAccessUserKeyFormInvalid)
        this.keyName = fileName;
    }

  }

  refreshGrid() {
    this.refresh.buildGrid();
  }

  //
  // Private Methods
  //

  private checkInfrastructureCreationProgress() {
    this.userAccessKeyService.checkUserAccessKey()
      .subscribe(
      status => {
        if(status == 200)
        {
          if (this.preloaderModal.isOpened) {
            this.preloaderModal.close();
            clearInterval(this.preloadModalInterval);
          }
        } else if (status == 201)
        {
          if (this.keyUploadModal.isOpened)
            this.keyUploadModal.close();

          if (!this.preloaderModal.isOpened)
            this.preloaderModal.open({ isHeader: false, isFooter: false });
        }
      },
      err => {
        if (err.status == 404) // key haven't been uploaded
        {
          if (!this.keyUploadModal.isOpened)
            this.keyUploadModal.open({ isFooter: false });
        }
      }
      );
  }


  logout() {
    this.authenticationService.logout().subscribe(
      data => data,
      error => console.log(error),
      () => this.appRoutingService.redirectToLoginPage());
  }

  uploadUserAccessKey($event) {
    this.preloadModalInterval = setInterval(function() {
      this.checkInfrastructureCreationProgress();
    }.bind(this), 10000);
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
          console.log("shapes !!!", data);
          this.shapes = data
        },
        error => this.shapes = [{shape_name: 'M4.large'}, {shape_name: 'M4.large'}]
      );
  }

  createUsernotebook(tmplIndex, name, shape){
    this.userResourceService
      .createUsernotebook({
        name: name,
        shape: shape,
        image: this.createTempls[tmplIndex].image,
        version: this.createTempls[tmplIndex].version
      })
      .subscribe((result) => {
        console.log('result: ', result);

      });
      return false;
  };

  createEmr(template, name, shape){
    this.userResourceService
      .createUsernotebook({"image": name.value})
      .subscribe((result) => {
        console.log('result: ', result);
      });
    return false;
  };
}

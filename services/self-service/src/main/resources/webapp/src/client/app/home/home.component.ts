import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from './../security/authentication.service';
import { UserAccessKeyService } from "../services/userAccessKey.service";
import { UserResourceService } from "../services/userResource.service";
import { AppRoutingService } from "../routing/appRouting.service";
import { Http, Response } from '@angular/http';

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [AuthenticationService]
})

export class HomeComponent implements OnInit {
  key: any;
  uploadAccessKeyUrl: string;
  preloadModalInterval: any;

  templatesList: any;
  shapesList: any;



  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;

  // -------------------------------------------------------------------------
  // Overrides
  // --

  constructor(
    private authenticationService: AuthenticationService,
    private userAccessKeyProfileService: UserAccessKeyService,
    private userResourceService: UserResourceService,
    private appRoutingService: AppRoutingService,
    private http: Http
  ) {
    this.uploadAccessKeyUrl = this.userAccessKeyProfileService.getAccessKeyUrl();
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

  uploadUserAccessKey_btnClick($event) {
    this.preloadModalInterval = setInterval(function() {
      this.checkInfrastructureCreationProgress();
    }.bind(this), 10000);
  }

  //
  // Private Methods
  //

  private checkInfrastructureCreationProgress() {
    this.userAccessKeyProfileService.checkUserAccessKey()
      .subscribe(
      data => {
        if (this.preloaderModal.isOpened) {
          this.preloaderModal.close();
          clearInterval(this.preloadModalInterval);
        }
      },
      err => {
        if (err.status == 404) // key haven't been uploaded
        {
          if (!this.keyUploadModal.isOpened)
            this.keyUploadModal.open({ isFooter: false });
        } else if (err.status == 406) // key is being uploaded in progress
        {
          if (this.keyUploadModal.isOpened)
            this.keyUploadModal.close();

          if (!this.preloaderModal.isOpened)
            this.preloaderModal.open({ isHeader: false, isFooter: false });
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
    this.userResourceService.getTemplates()
      .subscribe(
      data => {
        this.templatesList = data;
      }
      );
    this.userResourceService.getShapes()
      .subscribe(
      data => {
        this.shapesList = data;
      }
      );
  }

  createUsernotebook(template, name, shape) {
    this.userResourceService
      .createUsernotebook({"image": name.value})
      .subscribe((result) => {
        console.log('result: ', result);
      });
    return false;
  };
}

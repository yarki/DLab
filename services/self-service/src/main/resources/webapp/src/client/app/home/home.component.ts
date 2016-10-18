import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from './../security/authentication.service';
import {UserAccessKeyService} from "../services/userAccessKey.service";
import {AppRoutingService} from "../routing/appRouting.service";

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [AuthenticationService]
})

export class HomeComponent implements OnInit {
  key: any;
  uploadAccessKeyUrl : string;
  preloadModalInterval:any;
  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;

  // -------------------------------------------------------------------------
  // Overrides
  // --

  constructor(
    private authenticationService: AuthenticationService,
    private userAccessKeyProfileService: UserAccessKeyService,
    private appRoutingService : AppRoutingService
  )
  {
    this.uploadAccessKeyUrl = this.userAccessKeyProfileService.getAccessKeyUrl();
  }

  ngOnInit() {
    this.checkInfrastructureCreationProgress();
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
    this.preloadModalInterval = setInterval(function () {
      this.checkInfrastructureCreationProgress();
    }.bind(this), 10000);
  }

  //
  // Private Methods
  //

  private checkInfrastructureCreationProgress()
  {
    this.userAccessKeyProfileService.checkUserAccessKey()
      .subscribe(
        data => {
          if(this.preloaderModal.isOpened)
          {
            this.preloaderModal.close();
            clearInterval(this.preloadModalInterval);
          }
        },
        err => {
          if(err.status == 404) // key haven't been uploaded
          {
            if(!this.keyUploadModal.isOpened)
              this.keyUploadModal.open({isFooter: false});
          } else if (err.status == 406) // key is being uploaded in progress
          {
            if(this.keyUploadModal.isOpened)
              this.keyUploadModal.close();

            if(!this.preloaderModal.isOpened)
              this.preloaderModal.open({isHeader: false, isFooter: false});
          }
        }
      );
  }
}

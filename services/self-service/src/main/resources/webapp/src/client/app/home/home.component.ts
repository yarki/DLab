import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Http } from '@angular/http';
import { AuthenticationService } from './../security/authentication.service';
import { UserProfileService } from "../security/userProfile.service";
import {UserAccessKeyService} from "../services/userAccessKey.service";

@Component({
  moduleId: module.id,
  selector: 'sd-home',
  templateUrl: 'home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [AuthenticationService]
})

export class HomeComponent implements OnInit {
  key: any;
  keyStatus: number;

  @ViewChild('keyUploadModal') keyUploadModal;
  @ViewChild('preloaderModal') preloaderModal;

  constructor(
    private http: Http,
    private authenticationService: AuthenticationService,
    private router: Router,
    private userProfileService : UserProfileService,
    private userAccessKeyProfileService: UserAccessKeyService
    ) {}

   logout() {
     this.authenticationService.logout().subscribe(
       data => data,
       err => console.log(err),
       () => this.router.navigate(['/login']));
   }

  ngOnInit() {
    this.checkInfrastructureCreationProgress();
  }

  checkInfrastructureCreationProgress()
  {
    this.userAccessKeyProfileService.checkUserAccessKey()
      .subscribe(
        data => {
          this.key = data;
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

  uploadingKey(event) {
    setTimeout(function () {
      this.checkInfrastructureCreationProgress();
    }.bind(this), 10000);
  }
}

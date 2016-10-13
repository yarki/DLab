import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Http, Response } from '@angular/http';
import { Observable }     from 'rxjs/Observable';
import { AuthenticationService } from './../security/authentication.service';
import { UserProfileService } from "../security/userProfile.service";

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

  constructor(private http: Http, private authenticationService: AuthenticationService, private router: Router, private userProfileService : UserProfileService) {}

   logout() {
     this.authenticationService.logout();
     this.router.navigate(['/login']);
   }


  ngOnInit() {
    this.getMethod()
    .subscribe(
      data => {
        this.key = data;
      },
      err => {
        this.keyStatus = err.status;
        this.openModal('.upload_key');
      }
    );
  }

  uploadKey(event) {
    this.uploadPreloader();
  }

  uploadPreloader() {
    this.openModal('.loading_modal');
    setTimeout(function () {
      this.closeModal('.loading_modal');
    }.bind(this), 10000);
  }

  getMethod() {
    return this.http.get(`/api/keyloader=?access_token=${this.userProfileService.getAuthToken()}`).map(( res:Response ) => res.json());
  }

  openModal(className: string) {
    [].forEach.call(document.querySelectorAll('.modal'), function(modal) {
      modal.setAttribute('open', '');
    });
    document.querySelector(className).setAttribute('open', 'true');
  }

  closeModal(className: string) {
    document.querySelector(className).setAttribute('open', '');
  }
}

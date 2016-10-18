import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { HttpModule } from '@angular/http';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

import {
  LocationStrategy,
  HashLocationStrategy
} from '@angular/common';

import { AuthenticationService} from './security/authentication.service'
import { AuthorizationGuard } from './security/authorization.guard';
import { LoginModule } from './login/login.module';
import { HomeModule } from './home/home.module';
import {WebRequestHelper} from "./util/webRequestHelper.service";
import {UserProfileService} from "./security/userProfile.service";
import {FormsModule} from "@angular/forms";
import {UserAccessKeyService} from "./services/userAccessKey.service";
import {AppRoutingService} from "./routing/appRouting.service";

@NgModule({
  imports: [BrowserModule, HttpModule, RouterModule.forRoot(routes, { useHash: true }), LoginModule, HomeModule, FormsModule],
  declarations: [AppComponent],
  providers: [{
    provide: LocationStrategy,
    useClass: HashLocationStrategy,
    useValue: '<%= APP_BASE %>'
  },
    AuthenticationService,
    AuthorizationGuard,
    WebRequestHelper,
    UserProfileService,
    UserAccessKeyService,
    AppRoutingService],
  bootstrap: [AppComponent]

})

export class AppModule {}

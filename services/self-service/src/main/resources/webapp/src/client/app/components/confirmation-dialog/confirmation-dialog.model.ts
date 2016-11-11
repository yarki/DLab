import {ConfirmationDialogType} from "./confirmation-dialog-type.enum";
import {Observable} from "rxjs";
import {Response} from "@angular/http";
import {UserResourceService} from "../../services/userResource.service";

export class ConfirmationDialogModel {
  private title : string;
  private notebook: any;
  private confirmAction : Function;
  private userResourceService : UserResourceService;
  constructor(confirmationType : ConfirmationDialogType,
              notebook : any,
              fnProcessResults: any,
              fnProcessErrors: any,
              userResourceService : UserResourceService
              ) {
    this.userResourceService = userResourceService;
    this.setup(confirmationType, notebook, fnProcessResults, fnProcessErrors);
  }

  static getDefault () : ConfirmationDialogModel
  {
    return new
      ConfirmationDialogModel(
        ConfirmationDialogType.StopExploratory, {name: "", resources: []}, () => {},  () => {}, null);
  }

  private stopExploratory() : Observable<Response> {
    return this.userResourceService.suspendExploratoryEnvironment(this.notebook, "stop");
  }

  private terminateExploratory() : Observable<Response> {
    return this.userResourceService.suspendExploratoryEnvironment(this.notebook, "terminate")
  }

  private setup(confirmationType : ConfirmationDialogType, notebook : any, fnProcessResults : any, fnProcessErrors: any) : void
  {
    switch (confirmationType)
    {
      case ConfirmationDialogType.StopExploratory: {
        this.title = "Exploratory Environment will be stopped and all connected computational resources will be terminated.";
        this.notebook = notebook;
        this.confirmAction = () => this.stopExploratory()
          .subscribe((response : Response) => fnProcessResults(response),
            (response: Response) => fnProcessErrors(response));
      }
      break;
      case ConfirmationDialogType.TerminateExploratory: {
        this.title = "Exploratory Environment and all connected computational resources will be terminated.";
        this.notebook = notebook;
        this.confirmAction = () => this.terminateExploratory()
          .subscribe((response : Response) => fnProcessResults(response),
            (response: Response) => fnProcessErrors(response));
      }
      break;
      default: {
        this.title = "Exploratory Environment and all connected computational resources will be terminated.";
        this.notebook = notebook;
        this.confirmAction = () => this.stopExploratory()
          .subscribe((response : Response) => fnProcessResults(response),
            (response: Response) => fnProcessErrors(response));
      }
      break;
    }
  }
}

import {Component, Input, Output, EventEmitter, ElementRef, ViewChild} from "@angular/core";

@Component({
    moduleId: module.id,
    selector: 'modal',
    templateUrl: 'modal.component.html',
    styleUrls: ['./modal.component.css']
})
export class Modal {

    // -------------------------------------------------------------------------
    // Inputs
    // -------------------------------------------------------------------------

    @Input()
    modalClass: string;

    @Input()
    closeOnEscape: boolean = true;

    @Input()
    closeOnOutsideClick: boolean = true;

    @Input()
    title: string;

    @Input()
    hideCloseButton = false;

    @Input()
    cancelButtonLabel: string;

    @Input()
    submitButtonLabel: string;

    // -------------------------------------------------------------------------
    // Outputs
    // -------------------------------------------------------------------------

    @Output()
    onOpen = new EventEmitter(false);

    @Output()
    onClose = new EventEmitter(false);

    @Output()
    onSubmit = new EventEmitter(false);

    // -------------------------------------------------------------------------
    // Public properties
    // -------------------------------------------------------------------------

    isOpened = false;

    isHeader = true;
    isFooter = true;

    // -------------------------------------------------------------------------
    // Private properties
    // -------------------------------------------------------------------------

    @ViewChild("modalRoot")
    private modalRoot: ElementRef;

    private backdropElement: HTMLElement;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    constructor() {
        this.createBackDrop();
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    ngOnDestroy() {
        document.body.className = document.body.className.replace(/modal-open\b/, "");
        if (this.backdropElement && this.backdropElement.parentNode === document.body)
            document.body.removeChild(this.backdropElement);
    }

    // -------------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------------

    open(option: Object, ...args: any[]) {
        if (this.isOpened)
            return;
        //console.log('!!!!!!!!!!', this);
        
        if (option) {
            for(let key in option) {
                this[key] = option[key]
            }
        }

        this.isOpened = true;
        this.onOpen.emit(args);
        document.body.appendChild(this.backdropElement);
        window.setTimeout(() => this.modalRoot.nativeElement.focus(), 0);
        document.body.className += " modal-open";
    }

    close(...args: any[]) {
        if (!this.isOpened)
            return;
        
        this.isOpened = false;
        this.onClose.emit(args);
        document.body.removeChild(this.backdropElement);
        document.body.className = document.body.className.replace(/modal-open\b/, "");
    }

    // -------------------------------------------------------------------------
    // Private Methods
    // -------------------------------------------------------------------------

    private preventClosing(event: MouseEvent) {
        event.stopPropagation();
    }

    private createBackDrop() {
        this.backdropElement = document.createElement("div");
        this.backdropElement.classList.add("modal-backdrop");
        this.backdropElement.classList.add("fade");
        this.backdropElement.classList.add("in");
    }

}
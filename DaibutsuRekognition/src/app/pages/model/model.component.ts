import { Component, OnInit } from '@angular/core';
import {AwsService} from '../../service/aws.service';

@Component({
  selector: 'app-model',
  templateUrl: './model.component.html',
  styleUrls: ['./model.component.css']
})
export class ModelComponent implements OnInit {

  constructor(private awsService: AwsService) { }

  ngOnInit(): void {
    this.awsService.getProjectAll().subscribe(x => { 
      var listProjectElement = document.getElementById("listProject");

      if(listProjectElement != undefined) {
        for (let i = 0; i < x.length; i++) {
          // optionタグを作成する
          var option = document.createElement("option");
          option.text = x[i].id;
          option.value = x[i].id;
          // selectタグの子要素にoptionタグを追加する
          listProjectElement.appendChild(option);
        }
      }
    });
  }

  onCreateModel(){
    var now = new Date();
    var year = now.getFullYear();
    var month = now.getMonth()+1;
    var date = now.getDate();
    var hour = now.getHours();
    var min = now.getMinutes();
    var sec = now.getSeconds();
    var msec = now.getMilliseconds();

    var projectId = this.getProjectId();
    var versionName = projectId + "-" + year + month + date + "-" + hour + min + sec + msec;

    this.awsService.CreateModel(projectId, versionName).subscribe(x => { 
      console.log('Subscriber:', x);
      var element = document.getElementById("resultLog");
      if(element != undefined) {
        var resultLog =  element.innerHTML;
        element.innerHTML = resultLog + "\n" + x;
      }
    });
  }

  private getProjectId(){
    var listProjectIdElement = document.getElementById("listProject") as HTMLSelectElement;
    var projectId = "";

    if(listProjectIdElement.options.length > 0){
      projectId = listProjectIdElement.options[listProjectIdElement.selectedIndex].value;
    }

    return projectId;
  }
}

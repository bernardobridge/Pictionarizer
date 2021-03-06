import React from 'react';
import Word from '../interfaces/Word.interface';
import WordsDataService from '../api/WordsDataService';
import { Formik, Form, Field, ErrorMessage } from 'formik'; 
import IWordProps from '../interfaces/IWordProps.interface';
import IWordState from '../interfaces/IWordState.interface';
import { SMALL_INPUT_FIELD, TEXTAREA_COLS, TEXTAREA_ROWS } from '../Constants';
import { getLoginId } from '../LoginLocalStorage';

const loginState = Number(getLoginId());

class CreateWord extends React.Component<IWordProps, IWordState>{

  constructor(props: IWordProps){
    super(props)

    this.state = {
      wordId: this.props.match.params.id,
      wordData: {
        id: null,
        userId: 0,
        ownLangWordName: '',
        targetLangWordName: '',
        ownLangExSentence: '',
        targetLangExSentence: '',
        createdDate: new Date(),
        image: new File(["foo"], "foo.txt")
      }
    }

    this.onSubmit = this.onSubmit.bind(this)
    this.onChange = this.onChange.bind(this)
    this.validate = this.validate.bind(this)
    this.cancelCreate = this.cancelCreate.bind(this)
  }

  cancelCreate(){
    this.props.history.push('/')
  }

  validate(values: Word){
    let errors: Partial<Word> = {};
    if(values.targetLangWordName === ''){
      errors.targetLangWordName = "Enter a word in your target language"
    }
    if(values.ownLangWordName === ''){
      errors.ownLangWordName = "Enter a word in your own language"
    }
    if(values.targetLangExSentence === ''){
      errors.targetLangExSentence = "Enter an example sentence in your target language"
    }
    if(values.ownLangExSentence === ''){
      errors.ownLangExSentence = "Enter an example sentence in your own language"
    }
    if(this.state.wordData.image.name === 'foo.txt'){
      errors.targetLangWordName = 'Not only filling the text fields, you must choose an image as well'
    }

    return errors; 
  }

  onChange(e: { currentTarget: HTMLInputElement; }){
    const chosenFile = e.currentTarget.files[0];
    let tempWordData = this.state.wordData;
    tempWordData.image = chosenFile;  
    this.setState({wordData:tempWordData});
  }  

  async onSubmit(values: Word){
    let word = {
      ...values, 
      image: this.state.wordData.image, 
      id: this.state.wordData.id,
      userId: loginState
    };

    await WordsDataService.createWord(word)
    .then(() => this.props.history.push('/words'))       
  }
  
  render(){
    let { id, userId, ownLangWordName, 
      targetLangWordName, ownLangExSentence, 
      targetLangExSentence, createdDate, 
      image} 
      = this.state.wordData; 

    return(
      <div className="object-details">
        <h2>Create Word</h2>
        <div>
          <Formik
            initialValues={{ id, userId, ownLangWordName, 
              targetLangWordName, ownLangExSentence, 
              targetLangExSentence, createdDate, 
              image}}
            onSubmit={this.onSubmit}
            validate={this.validate}
            enableReinitialize={true}
          >
            {
              (props) => (
                <Form>
                  <ErrorMessage name="targetLangWordName" component="div" className="text-danger"/>
                  <fieldset className="form-group">
                    <Field type="text" name="targetLangWordName" 
                      placeholder="Word in your target language" size={SMALL_INPUT_FIELD}
                    />
                  </fieldset>
                  <ErrorMessage name="ownLangWordName" component="div" className="text-danger"/>
                  <fieldset className="form-group">
                    <Field type="text" name="ownLangWordName" 
                      placeholder="Word in your own language" size={SMALL_INPUT_FIELD}
                    />
                  </fieldset>
                  <ErrorMessage name="targetLangExSentence" component="div" className="text-danger"/>
                  <fieldset className="form-group">
                    <Field as="textarea" name="targetLangExSentence"
                      placeholder="Sentence in your target language" 
                      cols={TEXTAREA_COLS} rows={TEXTAREA_ROWS}
                    />
                  </fieldset>
                  <ErrorMessage name="ownLangExSentence" component="div" className="text-danger"/>
                  <fieldset className="form-group">
                    <Field as="textarea" name="ownLangExSentence"
                      placeholder="Sentence in your own language" 
                      cols={TEXTAREA_COLS} rows={TEXTAREA_ROWS}
                    />
                  </fieldset>
                  <fieldset disabled className="form-group">
                    <Field type="text" id="disabledTextInput" name="createdDate" placeholder="Date" size="60"/>
                  </fieldset>
                  <fieldset className="custom-file" >
                    <input className="custom-file-input" id="customFile" type="file" name="image" onChange={this.onChange}/>
                    <label className="custom-file-label half-width-in-form" >
                      {this.state.wordData.image.name === 'foo.txt'? 'Choose file': this.state.wordData.image.name}
                    </label>
                  </fieldset>
                  <br/><br/>
                  <button className="btn btn-secondary" onClick={() => this.cancelCreate()}>Cancel</button>&nbsp;
                  <button type="submit" className="btn btn-primary">Save</button>
                </Form>
              )
            }      
          </Formik>
        </div>
      </div>
    )
  }
}

export default CreateWord; 
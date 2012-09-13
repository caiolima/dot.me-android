package com.dot.me.utils;

import java.util.Vector;

import com.dot.me.model.Mensagem;

import android.os.Parcel;
import android.os.Parcelable;

public class MensageList implements Parcelable{
	private int mData;
	//private int mData;
	private Vector<Mensagem> mensagemList=new Vector<Mensagem>();
	
	public MensageList(Vector<Mensagem> list){
    	mensagemList=list;
    }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public static final Parcelable.Creator<MensageList> CREATOR
            = new Parcelable.Creator<MensageList>() {
        @Override
		public MensageList createFromParcel(Parcel in) {
            return new MensageList(in);
        }

        @Override
		public MensageList[] newArray(int size) {
            return new MensageList[size];
        }
    };
    
    private MensageList(Parcel in) {
        mData = in.readInt();
    }
    
    



	public Vector<Mensagem> getMensagemList() {
		return mensagemList;
	}

	public void setMensagemList(Vector<Mensagem> mensagemList) {
		this.mensagemList = mensagemList;
	}

}

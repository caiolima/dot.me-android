package com.dot.me.model.bd;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.os.Handler;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.Draft;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.FacebookGroup;
import com.dot.me.model.Label;
import com.dot.me.model.Mensagem;
import com.dot.me.model.PalavraChave;
import com.dot.me.model.TrendLocation;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.User;
import com.dot.me.utils.BlackListUtils;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.Menssage;
import com.dot.me.utils.SubjectMessage;
import com.dot.me.utils.TwitterUtils;

public class Facade {

	private static Facade singleton;
	private TwitterBD twitterBD;
	private PalavraChaveBD palavraChaveBD;
	private MarcadorBD marcadorBD;
	private MensagemBD mensagemBD;
	private TwitterSearchBD twitterSearchBD;
	private UserDB userBD;
	private BlackListDB blackListBd;
	private FacebookBD facebookBd;
	private FacebookGroupsBD facebookGroupBd;
	private CollumnConfigDao configDao;
	private DraftBD draftBD;
	private LocationSelectedBD locationBD;

	public static Facade getInstance(Context ctx) {
		if (singleton == null)
			singleton = new Facade(ctx);

		return singleton;
	}

	private Facade(Context ctx) {

		twitterBD = new TwitterBD(ctx);
		palavraChaveBD = new PalavraChaveBD(ctx);
		marcadorBD = new MarcadorBD(ctx);
		mensagemBD = new MensagemBD(ctx);
		twitterSearchBD = new TwitterSearchBD(ctx);
		userBD = new UserDB(ctx);
		blackListBd = new BlackListDB(ctx);
		facebookBd = new FacebookBD(ctx);
		facebookGroupBd = new FacebookGroupsBD(ctx);
		configDao = new CollumnConfigDao(ctx);
		draftBD = new DraftBD(ctx);
		locationBD = new LocationSelectedBD(ctx);

	}

	public static void destroy() {
		singleton = null;
	}

	public int insert(TwitterAccount u) {

		return twitterBD.insert(u);
	}

	public void logoutTwitter() {
		twitterBD.delete();
		mensagemBD.deleteAll(Mensagem.TIPO_STATUS);
		mensagemBD.deleteAll(Mensagem.TIPO_TWEET_SEARCH);

		configDao.deleteOfType("twitter");
		twitterSearchBD.deleteAll();
		TwitterUtils.logoutTwitter();
	}

	public void logoutFacebook() {
		facebookBd.delete();
		mensagemBD.deleteAll(Mensagem.TIPO_FACE_COMENTARIO);
		mensagemBD.deleteAll(Mensagem.TIPO_FACEBOOK_GROUP);
		mensagemBD.deleteAll(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
		mensagemBD.deleteAll(Mensagem.TIPO_NEWS_FEEDS);

		facebookGroupBd.deleteAll();
		configDao.deleteOfType("face");
		FacebookUtils.logoutFacebook();

	}

	public Vector<Account> lastSavedSession() {
		Vector<Account> retorno = new Vector<Account>();
		retorno.addAll(twitterBD.lastSavedSession());
		retorno.addAll(facebookBd.getAll());

		return retorno;
	}

	public int insert(PalavraChave p) {
		int resp = palavraChaveBD.existsPalavra(p.getConteudo());
		if (resp != Menssage.ERRO) {
			return resp;
		}
		return palavraChaveBD.insert(p);
	}

	public int conectaPalavra(int idPalavra, int idMarcador) {
		return palavraChaveBD.conectaPalavra(idPalavra, idMarcador);
	}

	public int insert(Label m) {
		return marcadorBD.insert(m);
	}

	public Vector<PalavraChave> getByMarcador(long idMarcador) {
		return palavraChaveBD.getByMarcador(idMarcador);
	}

	public Vector<Label> getAllMarcadores() {
		return marcadorBD.getAll();
	}

	public Label getOneMarcador(int id) {
		return marcadorBD.getOne(id);
	}

	public void deletePalavraByMarcador(int idMarcador) {
		palavraChaveBD.deletePalavraByMarcador(idMarcador);
	}

	public void deletAllMarcadores() {
		palavraChaveBD.deletAll();
		marcadorBD.deletAll();
	}

	public boolean insert(final Mensagem m) {
		if (mensagemBD.existsStatus(m.getIdMensagem(), m.getTipo())
				|| !BlackListUtils.validateMessage(getAllWordsInBlackList(), m))
			return false;

		mensagemBD.insert(m);

		return true;
	}

	public Vector<Mensagem> getAllMensagens() {
		try {
			return mensagemBD.getAllMensagens();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public boolean exsistsStatus(String id, int tipo) {
		return mensagemBD.existsStatus(id, tipo);
	}

	public void deletAllMensagem(int type) {
		mensagemBD.deleteAll(type);
	}

	public void deletAllMensagem() {
		mensagemBD.deleteAll();
	}

	public void deleteAllTo(long date) {
		mensagemBD.deleteAllTo(date);
	}

	public int getCountMensagem(int type) {
		return mensagemBD.count(type);
	}

	public Vector<Mensagem> getMensagemOf(int type) {
		return mensagemBD.getMensagemOf(type);
	}

	public int insert(String search) {

		return twitterSearchBD.insert(search.toLowerCase().trim());
	}

	public void deleteSearch(String search) {
		twitterSearchBD.delete(search);
	}

	public void deleteAllSearch() {
		mensagemBD.deleteAllSearch();
	}

	public Vector<String> getAllSearches() {
		return twitterSearchBD.getAll();
	}

	public boolean wasSearchAdded(String search) {
		return twitterSearchBD.wasAdded(search);
	}

	public void deleteMarcador(int id) {
		palavraChaveBD.desconectaPalavraFromMarcador(id);
		marcadorBD.delete(id);
	}

	public void deleteMensagem(final String id, final int type) {
		final SubjectMessage mSubject = TimelineActivity.getmSubject();
		Handler h = TimelineActivity.h;
		if (h != null) {
			h.post(new Runnable() {

				@Override
				public void run() {
					if (mSubject != null)
						mSubject.notifyMessageRemovedObservers(id, type);
				}
			});
		}

		mensagemBD.delete(id, type);
	}

	public Mensagem getOneMessage(String id, int type) {
		return mensagemBD.getOne(id, type);
	}

	public void desconectaPalavra(int idPalavra, int idMarcador) {
		palavraChaveBD.desconectaPalavra(idPalavra, idMarcador);
	}

	public int update(Label m, String[] palavras) {
		return marcadorBD.update(m, palavras);
	}

	public void manageMarkup(Label m) {
		marcadorBD.manageMarkup(m);
	}

	public int insert(User u) {
		int return_value = -1;

		User user = userBD.getOne(u.getId(), u.getType());

		if (user != null)
			return_value = user.getSysID();
		else
			return_value = userBD.insert(u);

		return return_value;
	}

	public User getOneUser(long id, int type) {
		try {
			return userBD.getOne(id, type);
		} catch (Exception e) {
			return null;
		}
		
	}

	public User getOneUser(int id) {
		try {
			return userBD.getOne(id);
		} catch (Exception e) {
			return null;
		}
	}

	public int existsPalavra(String word) {
		return palavraChaveBD.existsPalavra(word);
	}

	public void insertInBlackList(String word) {
		try {
			blackListBd.insert(word);
		} catch (Exception e) {

		}
	}

	public void deleteInBlackList(String word) {
		try {
			blackListBd.delete(word);
		} catch (Exception e) {

		}
	}

	public Vector<String> getAllWordsInBlackList() {
		try {
			return blackListBd.getAll();
		} catch (Exception e) {
			return new Vector<String>();
		}
	}

	public PalavraChave getOnePalavra(long id) {
		return palavraChaveBD.getOne(id);
	}

	public void insert(FacebookAccount acc) {
		facebookBd.insert(acc);
	}

	public FacebookAccount getOneFacebookAccount(long id) {
		return facebookBd.getOne(id);
	}

	public Vector<FacebookAccount> getAllFacebook() {
		return facebookBd.getAll();
	}

	public void insert(FacebookGroup fg) {
		facebookGroupBd.insert(fg);
	}

	public void deleteFacebookGroup(String id) {
		facebookGroupBd.delete(id);
	}

	public FacebookGroup getOneFacebookGroup(String id) {
		return facebookGroupBd.getOne(id);
	}

	public Vector<FacebookGroup> getAllFacebookGroups() {
		return facebookGroupBd.getAll();
	}

	public void deleteAllGroups() {
		facebookGroupBd.deleteAll();
	}

	public boolean existsGroup(String id) {
		return facebookGroupBd.existsGroup(id);
	}

	public void update(Mensagem m) {
		mensagemBD.update(m);
	}

	public void insert(CollumnConfig c) {
		if((c.getType().equals(CollumnConfig.FACEBOOK_COLLUMN)||
				c.getType().equals(CollumnConfig.ME)||
				c.getType().equals(CollumnConfig.TWITTER_COLLUMN))&&
				configDao.existsCollumnType(c.getType()))
			return;
		
		configDao.insert(c);
	}

	public void deleteCollum(int pos) {
		configDao.delete(pos);

	}

	public ArrayList<CollumnConfig> getAllConfig() {

		return configDao.getAll();

	}

	public CollumnConfig getOneConfig(int pos) {
		return configDao.getOne(pos);
	}

	public void update(CollumnConfig config) {
		configDao.update(config);
	}

	public void changePos(CollumnConfig config, int toPos) {
		configDao.changePos(config, toPos);
	}

	public void deleteAllCollumnConfig() {
		configDao.deleteAll();
	}

	public Vector<Mensagem> getMensagemOfLikeId(int type, String idLike) {
		return mensagemBD.getMensagemOfLikeId(type, idLike);
	}

	public boolean existsCollumnType(String type) {
		return configDao.existsCollumnType(type);
	}

	public void insert(Draft d) {
		draftBD.insert(d);
	}

	public void deleteDraft(String id) {
		draftBD.delete(id);
	}

	public Draft getOneDraft(String id) {
		return draftBD.getOne(id);
	}

	public boolean existsDraft(String id) {
		return draftBD.exists(id);
	}

	public void setLocation(TrendLocation l) {
		locationBD.deleteCurrent();
		locationBD.insert(l);
	}

	public void deleteCurrentLocation() {
		locationBD.deleteCurrent();
	}

	public TrendLocation getSavedTrend() {
		TrendLocation t = locationBD.getSaved();
		if (t == null) {
			t = new TrendLocation();
			t.setName("World");
			t.setWoeid(1);
		}

		return t;
	}
}

package org.apache.pig.contrib.eclipse.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pig.contrib.eclipse.OpenDeclarationHandler;
import org.apache.pig.contrib.eclipse.PigActivator;
import org.apache.pig.contrib.eclipse.PigPreferences;
import org.apache.pig.contrib.eclipse.utils.PigWordDetector;
import org.apache.pig.contrib.eclipse.utils.ResourceReader;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class PigScriptScanner extends RuleBasedScanner {

	private static Set<String> KEYWORDS;
	private static Set<String> BUILTIN_FUN;
	private static Set<String> DATA_TYPES;
	private static Map<String, String> KEYWORD_TOOLTIPS;
	private static Map<String, String> BUILTIN_TOOLTIPS;

	/**
	 * This function loads the correct resources for all the classes that need them
	 */
	public static void loadResources(String version) {
		KEYWORDS = ResourceReader.readKeywords(version);
		BUILTIN_FUN = ResourceReader.readBuiltIns(version);
		DATA_TYPES = ResourceReader.readDataTypes(version);
		KEYWORD_TOOLTIPS = ResourceReader.readKeywordTooltips(version, KEYWORDS);
		BUILTIN_TOOLTIPS = ResourceReader.readBuiltInTooltips(version, BUILTIN_FUN);
		
		PigContentAssistant.setResources(KEYWORDS, BUILTIN_FUN);
		OpenDeclarationHandler.setResources(BUILTIN_FUN, KEYWORD_TOOLTIPS, BUILTIN_TOOLTIPS);
	}
		
	public PigScriptScanner() {
		
		IPreferenceStore store = PigActivator.getDefault().getPreferenceStore();
		
		loadResources(store.getString(PigPreferences.PIG_VERSION));
		
		IToken keywordToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_KEYWORDS)), null, SWT.BOLD));
		
		IToken commentToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_COMMENTS))));
		
		IToken builtinFunToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_BUILTINS))));

		IToken constantToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_CONSTANTS))));

		IToken defaultToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_DEFAULT))));

		IToken dataTypeToken = new Token(new TextAttribute(getColor(PreferenceConverter.getColor(store, PigPreferences.COLOR_DATATYPES))));

		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new EndOfLineRule("--", commentToken)); //$NON-NLS-1$

		rules.add(new SingleLineRule("'", "'", constantToken, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		
		rules.add(new NumberRule(constantToken));

		rules.add(new MultiLineRule("/*", "*/", commentToken)); //$NON-NLS-2$ //$NON-NLS-1$
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PigWhiteSpaceDetector()));

		// Add word rule for built in functions
		WordRule caseSensitiveRule = new WordRule(PigWordDetector.INSTANCE, Token.UNDEFINED, false);

		for (String func : BUILTIN_FUN)
			caseSensitiveRule.addWord(func, builtinFunToken);

		rules.add(caseSensitiveRule);
		
		// Add word rule for keywords and data types
		WordRule caseInsensitiveRule = new WordRule(PigWordDetector.INSTANCE, defaultToken, true);
		
		for (String keyword : KEYWORDS)
			caseInsensitiveRule.addWord(keyword, keywordToken);


		for (String datatype : DATA_TYPES)
			caseInsensitiveRule.addWord(datatype, dataTypeToken);

		rules.add(caseInsensitiveRule);
		
		setRules(rules.toArray(new IRule[rules.size()]));
	}
	
	private static Color getColor(RGB rgb){
		return new Color(Display.getCurrent(),rgb);
	}

}

/**
 * 
 */
package com.pluseq.coreader;

/**
 * Registry of all components of CoReader
 * @author or
 * 
 */
public class PluginManager {
	public ParserAbstract getParserByFileName() {
		return new ParserText();
	}
}

package net.madz.download.service.metadata;

/**
 * Meta Data Module
 * 
 * Each download task is described by a meta data file, including information as following:
 * URL, File Folder, File Name, resumable, file size, segments, thread numbers, states and etc.
 * 
 * Meta data module maintains concurrent reads/writes from/to a certain download task 
 * meta data file.
 *  
 */
